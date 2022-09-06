package xyz.brassgoggledcoders.dailyresources.resource.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.dailyresources.capability.ResourceStorage;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesResources;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceStorageSelection;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceType;

public class FluidStackResourceStorage extends ResourceStorage {
    public static final Codec<FluidStackResourceStorage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FluidStackResourceFluidHandler.CODEC.fieldOf("fluidHandler").forGetter(FluidStackResourceStorage::getFluidHandler)
    ).apply(instance, FluidStackResourceStorage::new));

    private final FluidStackResourceFluidHandler fluidHandler;

    public FluidStackResourceStorage(FluidStackResourceFluidHandler fluidHandler) {
        this.fluidHandler = fluidHandler;
    }

    @Override
    public ResourceType<?> getResourceType() {
        return DailyResourcesResources.FLUIDSTACK.get();
    }

    @Override
    public void invalidateCapabilities() {

    }

    @Override
    public void trigger(ResourceStorageSelection<?> resourceStorageSelection) {

    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return LazyOptional.empty();
    }

    public FluidStackResourceFluidHandler getFluidHandler() {
        return fluidHandler;
    }
}
