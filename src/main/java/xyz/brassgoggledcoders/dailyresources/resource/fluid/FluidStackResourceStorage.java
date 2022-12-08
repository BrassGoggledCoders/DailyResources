package xyz.brassgoggledcoders.dailyresources.resource.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
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
    private final LazyOptional<IFluidHandler> lazyOptional = LazyOptional.of(this::getFluidHandler);

    public FluidStackResourceStorage(FluidStackResourceFluidHandler fluidHandler) {
        this.fluidHandler = fluidHandler;
    }

    @Override
    public ResourceType<?> getResourceType() {
        return DailyResourcesResources.FLUIDSTACK.get();
    }

    @Override
    public void invalidateCapabilities() {
        this.lazyOptional.invalidate();
    }

    @Override
    public boolean trigger(ResourceStorageSelection<?> resourceStorageSelection) {
        return resourceStorageSelection.getChoice(DailyResourcesResources.FLUIDSTACK.get())
                .map(fluidStack -> this.fluidHandler.fill(fluidStack.copy(), FluidAction.EXECUTE) != fluidStack.getAmount())
                .orElse(false);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, this.lazyOptional);
    }

    public FluidStackResourceFluidHandler getFluidHandler() {
        return fluidHandler;
    }
}
