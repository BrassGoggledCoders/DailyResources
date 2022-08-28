package xyz.brassgoggledcoders.dailyresources.trigger;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesTriggers;

public class Trigger extends ForgeRegistryEntry<Trigger> {
    private final ResourceLocation texture;

    public Trigger(ResourceLocation texture) {
        this.texture = texture;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public Trigger merge(@NotNull Trigger other) {
        if (DailyResourcesTriggers.ANY.is(this)) {
            return other;
        } else if (DailyResourcesTriggers.ANY.is(other)) {
            return this;
        } else {
            return other == this ? this : null;
        }
    }
}
