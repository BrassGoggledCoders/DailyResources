package xyz.brassgoggledcoders.dailyresources.capability;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import xyz.brassgoggledcoders.dailyresources.codec.Codecs;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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

    private final Map<UUID, ResourceStorage> resourceStorages;

    public ResourceStorageStorage() {
        this.resourceStorages = Maps.newHashMap();
    }

    public ResourceStorageStorage(Map<UUID, ResourceStorage> resourceStorages) {
        this.resourceStorages = new HashMap<>(resourceStorages);
    }

    public boolean hasResourceSource(UUID uniqueId) {
        return resourceStorages.containsKey(uniqueId);
    }

    public void createResourceStorage(UUID uniqueId, ResourceStorage storage) {
        if (storage.isValid()) {
            this.resourceStorages.put(uniqueId, storage);
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
}
