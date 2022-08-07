package xyz.brassgoggledcoders.dailyresources.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.KeyDispatchCodec;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class OptionalTypeKeyDispatchCodec<K, V> extends KeyDispatchCodec<K, V> {
    private final String typeKey;
    private final String defaultValue;

    public OptionalTypeKeyDispatchCodec(String defaultValue, final String typeKey,
                                        final Codec<K> keyCodec,
                                        final Function<? super V, ? extends K> type,
                                        final Function<? super K, ? extends Codec<? extends V>> codec
    ) {
        super(typeKey, keyCodec, type.andThen(DataResult::success), codec.andThen(DataResult::success));
        this.typeKey = typeKey;
        this.defaultValue = defaultValue;
    }

    @Override
    public <T> DataResult<V> decode(final DynamicOps<T> ops, final MapLike<T> input) {
        return super.decode(
                ops,
                new DefaultingMapLike<>(input, typeKey, () -> ops.createString(defaultValue))
        );
    }
}

class DefaultingMapLike<T> implements MapLike<T> {
    private final MapLike<T> input;
    private final String typeKey;
    private final Supplier<T> defaultValue;

    DefaultingMapLike(MapLike<T> input, String typeKey, Supplier<T> defaultValue) {
        this.input = input;
        this.typeKey = typeKey;
        this.defaultValue = defaultValue;
    }

    @Nullable
    @Override
    public T get(T key) {
        return input.get(key);
    }

    @Nullable
    @Override
    public T get(String key) {
        T value = input.get(key);
        if (value == null && key.equals(typeKey)) {
            return this.defaultValue.get();
        }
        return value;
    }

    @Override
    public Stream<Pair<T, T>> entries() {
        return input.entries();
    }
}
