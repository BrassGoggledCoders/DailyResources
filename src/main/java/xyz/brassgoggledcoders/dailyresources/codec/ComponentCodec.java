package xyz.brassgoggledcoders.dailyresources.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;

public class ComponentCodec implements Codec<Component> {
    @Override
    public <T> DataResult<Pair<Component, T>> decode(DynamicOps<T> ops, T input) {
        JsonElement element = ops.convertTo(JsonOps.INSTANCE, input);

        try {
            return DataResult.success(Pair.of(Component.Serializer.fromJson(element), input));
        } catch (JsonSyntaxException e) {
            return DataResult.error(e.getMessage());
        }
    }

    @Override
    public <T> DataResult<T> encode(Component input, DynamicOps<T> ops, T prefix) {
        JsonElement jsonElement = Component.Serializer.toJsonTree(input);
        return DataResult.success(JsonOps.INSTANCE.convertTo(ops, jsonElement));
    }
}
