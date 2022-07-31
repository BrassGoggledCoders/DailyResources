package xyz.brassgoggledcoders.dailyresources.capability;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesResources;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceStorageInfo;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceType;

import java.util.function.Supplier;

public abstract class ResourceStorage implements ICapabilityProvider {
    public static Supplier<Codec<ResourceStorage>> CODEC = Suppliers.memoize(() -> DailyResourcesResources.REGISTRY.get()
            .getCodec()
            .dispatch(ResourceStorage::getResourceType, ResourceType::getStorageCodec)
    );

    private final ResourceStorageInfo info;

    public ResourceStorage(ResourceStorageInfo info) {
        this.info = info;
    }

    public ResourceStorageInfo getInfo() {
        return info;
    }

    public boolean isValid() {
        return true;
    }

    public abstract ResourceType getResourceType();

    public abstract void invalidateCapabilities();
}
