package xyz.brassgoggledcoders.dailyresources.codec;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import xyz.brassgoggledcoders.dailyresources.resource.ListenerType;

import java.util.UUID;

public class Codecs {
    public static final Codec<UUID> UNIQUE_ID = Codec.STRING.xmap(
            UUID::fromString,
            UUID::toString
    );

    public static final Codec<ListenerType> LISTENER_TYPE = Codec.STRING.xmap(
            ListenerType::valueOf,
            ListenerType::name
    );

    public static final ComponentCodec COMPONENT = new ComponentCodec();
}
