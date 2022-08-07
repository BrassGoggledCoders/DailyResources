package xyz.brassgoggledcoders.dailyresources.trigger;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class Trigger extends ForgeRegistryEntry<Trigger> {
    private final ResourceLocation texture;

    public Trigger(ResourceLocation texture) {
        this.texture = texture;
    }

    public ResourceLocation getTexture() {
        return texture;
    }
}
