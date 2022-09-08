package xyz.brassgoggledcoders.dailyresources.capability;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.dailyresources.DailyResources;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CapabilityProvider<T, U extends T> implements ICapabilitySerializable<Tag> {
    private final Capability<T> capability;
    private final Codec<U> codec;
    private final LazyOptional<T> lazyOptional;
    private final Supplier<U> defaultSupplier;
    private final Consumer<U> onInvalidate;

    private final BiConsumer<T, MinecraftServer> setServer;

    private final MinecraftServer minecraftServer;

    private U value;

    public CapabilityProvider(Capability<T> capability, Codec<U> codec, Supplier<U> defaultSupplier, Consumer<U> onInvalidate,
                              BiConsumer<T, MinecraftServer> setServer, MinecraftServer minecraftServer) {
        this.capability = capability;
        this.codec = codec;
        this.defaultSupplier = defaultSupplier;
        this.onInvalidate = onInvalidate;
        this.minecraftServer = minecraftServer;
        this.setServer = setServer;
        this.lazyOptional = LazyOptional.of(this::getValue);
    }

    private U getValue() {
        if (this.value == null) {
            this.value = defaultSupplier.get();
            this.setServer.accept(this.value, this.minecraftServer);
        }
        return this.value;
    }

    @NotNull
    @Override
    public <C> LazyOptional<C> getCapability(@NotNull Capability<C> cap, @Nullable Direction side) {
        return this.capability.orEmpty(cap, this.lazyOptional);
    }

    @Override
    public Tag serializeNBT() {
        return codec.encodeStart(NbtOps.INSTANCE, this.getValue())
                .getOrThrow(false, error -> DailyResources.LOGGER.error("Failed to Serialize: %s".formatted(error)));
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        this.value = codec.decode(NbtOps.INSTANCE, nbt)
                .getOrThrow(false, error -> DailyResources.LOGGER.error("Failed to Deserialize: %s".formatted(error)))
                .getFirst();
        this.setServer.accept(this.value, this.minecraftServer);
    }

    public void invalidate() {
        this.lazyOptional.invalidate();
        this.onInvalidate.accept(this.getValue());
    }
}
