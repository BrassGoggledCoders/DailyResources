package xyz.brassgoggledcoders.dailyresources.codec;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import xyz.brassgoggledcoders.dailyresources.resource.ListenedEvent;

import java.util.UUID;

public class Codecs {
    public static final Codec<UUID> UNIQUE_ID = Codec.STRING.xmap(
            UUID::fromString,
            UUID::toString
    );

    public static final Codec<Ingredient> INGREDIENT = new JsonCodec<>(
            Ingredient::fromJson,
            Ingredient::toJson
    );

    public static final Codec<Component> COMPONENT = new JsonCodec<>(
            Component.Serializer::fromJson,
            Component.Serializer::toJsonTree
    );
}
