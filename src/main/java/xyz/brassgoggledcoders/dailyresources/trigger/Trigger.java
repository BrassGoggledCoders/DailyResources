package xyz.brassgoggledcoders.dailyresources.trigger;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesTriggers;

public record Trigger(ResourceLocation texture) {

    public Trigger merge(@NotNull Trigger other) {
        if (DailyResourcesTriggers.ANY.is(this)) {
            return other;
        } else if (DailyResourcesTriggers.ANY.is(other)) {
            return this;
        } else {
            return other == this ? this : DailyResourcesTriggers.NONE.get();
        }
    }
}
