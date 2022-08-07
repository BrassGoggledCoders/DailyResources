package xyz.brassgoggledcoders.dailyresources.capability;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.codec.Codecs;
import xyz.brassgoggledcoders.dailyresources.trigger.Trigger;

import java.util.*;
import java.util.function.Supplier;

public class ResourceStorageStorage {
    public static Capability<ResourceStorageStorage> CAP = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static Supplier<Codec<ResourceStorageStorage>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.unboundedMap(Codecs.UNIQUE_ID, ResourceStorage.CODEC.get())
                            .fieldOf("resourceStorages")
                            .forGetter(ResourceStorageStorage::getResourceStorages)
            ).apply(instance, ResourceStorageStorage::new))
    );

    private int generation;

    private final Map<UUID, ResourceStorage> resourceStorages;
    private final Map<Trigger, List<Pair<UUID, UUID>>> cachedTriggered;

    public ResourceStorageStorage() {
        this.resourceStorages = new HashMap<>();
        this.cachedTriggered = new IdentityHashMap<>();
    }

    public ResourceStorageStorage(Map<UUID, ResourceStorage> resourceStorages) {
        this.resourceStorages = new HashMap<>(resourceStorages);
        this.cachedTriggered = new IdentityHashMap<>();
    }

    public boolean hasResourceSource(UUID uniqueId) {
        return resourceStorages.containsKey(uniqueId);
    }

    public void createResourceStorage(UUID uniqueId, ResourceStorage storage) {
        if (storage.isValid()) {
            this.resourceStorages.put(uniqueId, storage);
            DailyResources.RESOURCE_GROUP_MANAGER.getEntry(storage.getInfo().resourceGroupId())
                    .ifPresent(resourceGroup -> {
                        if (cachedTriggered.containsKey(resourceGroup.trigger())) {
                            cachedTriggered.get(resourceGroup.trigger())
                                    .add(Pair.of(storage.getInfo().owner(), uniqueId));
                        }
                    });
        }
    }

    public ResourceStorage getResourceStorage(UUID uniqueId) {
        return this.resourceStorages.get(uniqueId);
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
                .filter(entry -> players.isEmpty() || players.contains(entry.getFirst()))
                .map(entry -> this.getResourceStorage(entry.getSecond()))
                .filter(Objects::nonNull)
                .forEach(ResourceStorage::trigger);
    }

    private List<Pair<UUID, UUID>> collectToBeTriggered(Trigger trigger) {
        List<ResourceLocation> resourceTriggers = DailyResources.RESOURCE_GROUP_MANAGER.getEntries()
                .filter(entry -> entry.getValue().trigger() == trigger)
                .map(Map.Entry::getKey)
                .toList();

        return this.resourceStorages.entrySet()
                .parallelStream()
                .filter(entry -> resourceTriggers.contains(entry.getValue().getInfo().resourceGroupId()))
                .map(entry -> Pair.of(entry.getValue().getInfo().owner(), entry.getKey()))
                .toList();
    }
}
