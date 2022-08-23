package xyz.brassgoggledcoders.dailyresources.capability;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesResources;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceStorageSelection;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class ResourceStorage implements ICapabilityProvider {
    public static Supplier<Codec<ResourceStorage>> CODEC = Suppliers.memoize(() -> DailyResourcesResources.REGISTRY.get()
            .getCodec()
            .dispatch(ResourceStorage::getResourceType, ResourceType::getStorageCodec)
    );

    private final Map<UUID, ResourceStorageSelection<?>> selections;

    public ResourceStorage() {
        this.selections = new HashMap<>();
    }

    public Collection<ResourceStorageSelection<?>> getSelections() {
        return selections.values();
    }

    public ResourceStorageSelection<?> getSelection(UUID uniqueId) {
        return this.selections.get(uniqueId);
    }

    public abstract ResourceType<?> getResourceType();

    public abstract void invalidateCapabilities();

    public abstract void trigger(ResourceStorageSelection<?> resourceStorageSelection);

    public boolean addSelection(ResourceStorageSelection<?> resourceSelection) {
        if (this.getSelection(resourceSelection.id()) != null) {
            return false;
        } else {
            this.selections.put(resourceSelection.id(), resourceSelection);
            return true;
        }
    }

    public boolean hasSelection(UUID uuid) {
        return this.selections.containsKey(uuid);
    }
}
