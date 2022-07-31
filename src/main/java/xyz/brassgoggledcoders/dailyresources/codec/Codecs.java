package xyz.brassgoggledcoders.dailyresources.codec;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.UUID;

public class Codecs {
    public static final Codec<UUID> UNIQUE_ID = Codec.STRING.xmap(
            UUID::fromString,
            UUID::toString
    );
}
