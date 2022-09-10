package xyz.brassgoggledcoders.dailyresources.codec;

import com.mojang.serialization.Codec;
import xyz.brassgoggledcoders.dailyresources.resource.ListenedEvent;

import java.util.UUID;

public class Codecs {
    public static final Codec<UUID> UNIQUE_ID = Codec.STRING.xmap(
            UUID::fromString,
            UUID::toString
    );

    public static final ComponentCodec COMPONENT = new ComponentCodec();
}
