package xyz.brassgoggledcoders.dailyresources.resource;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.ForgeRegistryEntry;
import xyz.brassgoggledcoders.dailyresources.capability.ResourceStorage;

public class ResourceType extends ForgeRegistryEntry<ResourceType> {
    private final Codec<? extends Resource> resourceCodec;
    private final Codec<? extends ResourceStorage> storageCodec;

    public ResourceType(Codec<? extends Resource> resourceCodec, Codec<? extends ResourceStorage> storageCodec) {
        this.resourceCodec = resourceCodec;
        this.storageCodec = storageCodec;
    }

    public Codec<? extends Resource> getResourceCodec() {
        return resourceCodec;
    }

    public Codec<? extends ResourceStorage> getStorageCodec() {
        return this.storageCodec;
    }
}
