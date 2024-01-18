package xyz.brassgoggledcoders.dailyresources.capability;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.blockentity.IResourceListener;
import xyz.brassgoggledcoders.dailyresources.codec.Codecs;
import xyz.brassgoggledcoders.dailyresources.resource.ListenedEvent;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceStorageSelection;
import xyz.brassgoggledcoders.dailyresources.trigger.Trigger;
import xyz.brassgoggledcoders.dailyresources.trigger.TriggerInfo;

import java.util.*;
import java.util.function.Supplier;

public class ResourceStorageStorage {
    public static Capability<ResourceStorageStorage> CAP = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static Supplier<Codec<ResourceStorageStorage>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.unboundedMap(Codecs.UNIQUE_ID, ResourceStorage.STORAGE_CODEC.get())
                            .fieldOf("resourceStorages")
                            .forGetter(ResourceStorageStorage::getResourceStorages),
                    Codec.unboundedMap(Codecs.UNIQUE_ID, Codec.list(ResourceStorageSelection.CODEC.get()))
                            .fieldOf("resourceSelections")
                            .forGetter(ResourceStorageStorage::getSelections),
                    Codec.optionalField("resourceListener", Codec.unboundedMap(
                                    Codecs.UNIQUE_ID,
                                    Codec.unboundedMap(
                                            ResourceKey.codec(Registry.DIMENSION_REGISTRY),
                                            BlockPos.CODEC.listOf()
                                    )
                            ))
                            .forGetter(resourceStorageStorage -> Optional.of(resourceStorageStorage.getListeners()))
            ).apply(instance, (storage, selection, listeners) -> ResourceStorageStorage.read(storage, selection, listeners.orElseGet(HashMap::new))))
    );

    private int generation;
    private MinecraftServer minecraftServer;

    private final Map<UUID, ResourceStorage> resourceStorages;
    private final Map<Trigger, List<TriggerInfo>> cachedTriggered;

    public ResourceStorageStorage() {
        this.resourceStorages = new HashMap<>();
        this.cachedTriggered = new IdentityHashMap<>();
    }

    public ResourceStorageStorage(Map<UUID, ResourceStorage> resourceStorages) {
        this.resourceStorages = resourceStorages;
        this.cachedTriggered = new IdentityHashMap<>();
    }

    public ResourceStorage createResourceStorage(UUID uniqueId, ResourceStorage storage) {
        this.resourceStorages.put(uniqueId, storage);
        return storage;
    }

    public ResourceStorage getResourceStorage(UUID uniqueId) {
        return this.resourceStorages.get(uniqueId);
    }

    public ResourceStorage getOrCreateResourceStorage(UUID uniqueId, Supplier<ResourceStorage> creator) {
        ResourceStorage resourceStorage = this.getResourceStorage(uniqueId);
        if (resourceStorage == null) {
            resourceStorage = this.createResourceStorage(uniqueId, creator.get());
            cachedTriggered.clear();
        }
        return resourceStorage;
    }

    public <T> LazyOptional<T> getCapability(UUID uuid, Capability<T> capability) {
        ResourceStorage resourceStorage = this.getResourceStorage(uuid);
        if (resourceStorage == null) {
            return LazyOptional.empty();
        } else {
            return resourceStorage.getCapability(capability);
        }
    }

    private Map<UUID, ResourceStorage> getResourceStorages() {
        return this.resourceStorages;
    }

    public void trigger(Supplier<Trigger> trigger, Collection<UUID> players) {
        this.trigger(trigger.get(), players);
    }

    public void trigger(Trigger trigger, Collection<UUID> players) {
        if (this.generation != DailyResources.RESOURCE_GROUP_MANAGER.getGeneration()) {
            this.generation = DailyResources.RESOURCE_GROUP_MANAGER.getGeneration();
            this.cachedTriggered.clear();
        }


        cachedTriggered.computeIfAbsent(trigger, this::collectToBeTriggered)
                .stream()
                .filter(entry -> players.isEmpty() || players.contains(entry.chosenById()))
                .forEach(entry -> {
                    ResourceStorage resourceStorage = this.getResourceStorage(entry.resourceStorageId());
                    if (resourceStorage != null) {
                        ResourceStorageSelection<?> selection = resourceStorage.getSelection(entry.selectionId());
                        if (selection != null) {
                            if (resourceStorage.trigger(selection)) {
                                alertListener(ListenedEvent.UPDATE, resourceStorage);
                                alertListener(ListenedEvent.FULL, resourceStorage);
                            } else {
                                alertListener(ListenedEvent.UPDATE, resourceStorage);
                            }
                        }
                    }
                });
    }

    private void alertListener(ListenedEvent listenedEvent, ResourceStorage resourceStorage) {
        Map<ResourceKey<Level>, List<BlockPos>> listeners = resourceStorage.getListeners();
        if (!listeners.isEmpty() && minecraftServer != null) {
            for (Map.Entry<ResourceKey<Level>, List<BlockPos>> listenerEntry : listeners.entrySet()) {
                ServerLevel level = minecraftServer.getLevel(listenerEntry.getKey());
                if (level != null) {
                    for (BlockPos blockPos : listenerEntry.getValue()) {
                        if (level.isLoaded(blockPos)) {
                            if (level.getBlockEntity(blockPos) instanceof IResourceListener resourceListener) {
                                resourceListener.onEvent(listenedEvent);
                            }
                        }
                    }
                }
            }
        }
    }

    private List<TriggerInfo> collectToBeTriggered(Trigger trigger) {
        List<ResourceLocation> resourceTriggers = DailyResources.RESOURCE_GROUP_MANAGER.getEntries()
                .filter(entry -> entry.getValue().trigger() == trigger)
                .map(Map.Entry::getKey)
                .toList();

        return this.resourceStorages.entrySet()
                .parallelStream()
                .flatMap(entry -> entry.getValue()
                        .getSelections()
                        .stream()
                        .filter(resourceSelection -> resourceTriggers.contains(resourceSelection.resourceGroupId()))
                        .map(selection -> new TriggerInfo(
                                entry.getKey(),
                                selection.id(),
                                selection.chosenBy()
                        ))
                )
                .toList();
    }

    private Map<UUID, List<ResourceStorageSelection<?>>> getSelections() {
        Map<UUID, List<ResourceStorageSelection<?>>> selections = Maps.newHashMap();
        for (Map.Entry<UUID, ResourceStorage> resourceStorageEntry : this.resourceStorages.entrySet()) {
            if (!resourceStorageEntry.getValue().getSelections().isEmpty()) {
                selections.put(
                        resourceStorageEntry.getKey(),
                        new ArrayList<>(resourceStorageEntry.getValue().getSelections())
                );
            }
        }
        return selections;
    }

    private Map<UUID, Map<ResourceKey<Level>, List<BlockPos>>> getListeners() {
        Map<UUID, Map<ResourceKey<Level>, List<BlockPos>>> storageListeners = new HashMap<>();
        for (Map.Entry<UUID, ResourceStorage> resourceStorageEntry : this.resourceStorages.entrySet()) {
            storageListeners.put(resourceStorageEntry.getKey(), resourceStorageEntry.getValue().getListeners());
        }
        return storageListeners;
    }

    public void resetCache() {
        this.cachedTriggered.clear();
    }

    public void invalidate() {
        this.resourceStorages.values().forEach(ResourceStorage::invalidateCapabilities);
    }

    public void attemptAddListener(UUID storageId, Level level, BlockPos blockPos) {
        ResourceStorage resourceStorage = this.getResourceStorage(storageId);
        if (resourceStorage != null) {
            resourceStorage.addListener(level, blockPos);
        }
    }

    public void setMinecraftServer(MinecraftServer minecraftServer) {
        this.minecraftServer = minecraftServer;
    }

    public void attemptRemoveListener(UUID uniqueId, Level level, BlockPos blockPos) {
        ResourceStorage resourceStorage = this.getResourceStorage(uniqueId);
        if (resourceStorage != null) {
            resourceStorage.removeListener(level, blockPos);
        }
    }

    public static ResourceStorageStorage read(
            Map<UUID, ResourceStorage> storages,
            Map<UUID, List<ResourceStorageSelection<?>>> selections,
            Map<UUID, Map<ResourceKey<Level>, List<BlockPos>>> allListeners
    ) {
        for (Map.Entry<UUID, ResourceStorage> entry : storages.entrySet()) {
            List<ResourceStorageSelection<?>> list = selections.get(entry.getKey());
            if (list != null) {
                list.forEach(entry.getValue()::addSelection);
            }
            Map<ResourceKey<Level>, List<BlockPos>> listeners = allListeners.get(entry.getKey());
            if (listeners != null) {
                for (Map.Entry<ResourceKey<Level>, List<BlockPos>> listenerEntry : listeners.entrySet()) {
                    entry.getValue().addListeners(listenerEntry.getKey(), listenerEntry.getValue());
                }
            }
        }

        return new ResourceStorageStorage(
                new HashMap<>(storages)
        );
    }
}
