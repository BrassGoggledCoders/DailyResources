package xyz.brassgoggledcoders.dailyresources.capability;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.codec.Codecs;
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
                    Codec.unboundedMap(Codecs.UNIQUE_ID, ResourceStorage.CODEC.get())
                            .fieldOf("resourceStorages")
                            .forGetter(ResourceStorageStorage::getResourceStorages),
                    Codec.unboundedMap(Codecs.UNIQUE_ID, Codec.list(ResourceStorageSelection.CODEC.get()))
                            .fieldOf("resourceSelections")
                            .forGetter(ResourceStorageStorage::getSelections)
            ).apply(instance, ResourceStorageStorage::read))
    );

    private int generation;

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
                .parallelStream()
                .filter(entry -> players.isEmpty() || players.contains(entry.chosenById()))
                .forEach(entry -> {
                    ResourceStorage resourceStorage = this.getResourceStorage(entry.resourceStorageId());
                    if (resourceStorage != null) {
                        ResourceStorageSelection<?> selection = resourceStorage.getSelection(entry.selectionId());
                        if (selection != null) {
                            resourceStorage.trigger(selection);
                        }
                    }
                });
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

    public void invalidate() {
        this.resourceStorages.values().forEach(ResourceStorage::invalidateCapabilities);
    }

    public static ResourceStorageStorage read(
            Map<UUID, ResourceStorage> storages,
            Map<UUID, List<ResourceStorageSelection<?>>> selections
    ) {
        for (Map.Entry<UUID, ResourceStorage> entry : storages.entrySet()) {
            List<ResourceStorageSelection<?>> list = selections.get(entry.getKey());
            if (list != null) {
                list.forEach(entry.getValue()::addSelection);
            }
        }

        return new ResourceStorageStorage(
                new HashMap<>(storages)
        );
    }
}
