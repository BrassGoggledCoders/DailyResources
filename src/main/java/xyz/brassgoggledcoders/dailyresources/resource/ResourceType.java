package xyz.brassgoggledcoders.dailyresources.resource;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ResourceType extends ForgeRegistryEntry<ResourceType> {
    private final Codec<? extends Resource> codec;

    public ResourceType(Codec<? extends Resource> codec) {
        this.codec = codec;
    }

    public Codec<? extends Resource> getCodec() {
        return codec;
    }
}
