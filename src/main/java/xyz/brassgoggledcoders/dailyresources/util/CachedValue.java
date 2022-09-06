package xyz.brassgoggledcoders.dailyresources.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class CachedValue<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private final LongSupplier getTime;
    private final int maxTime;

    private long lastTime;
    private T value;

    public CachedValue(int maxTime, Supplier<T> get, LongSupplier getTime) {
        this.supplier = get;
        this.getTime = getTime;
        this.maxTime = maxTime;
    }

    @Override
    @NotNull
    public T get() {
        long time = this.getTime.getAsLong();
        if (this.value == null || time > this.lastTime + maxTime) {
            this.value = this.supplier.get();
            this.lastTime = time;
        }
        return this.value;
    }

    public void clear() {
        this.value = null;
    }
}
