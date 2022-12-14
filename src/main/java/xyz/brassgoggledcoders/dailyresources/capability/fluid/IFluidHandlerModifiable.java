package xyz.brassgoggledcoders.dailyresources.capability.fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;

public interface IFluidHandlerModifiable extends IFluidHandler {
    void setFluidInTank(int tank, @Nonnull FluidStack stack);

    int getSignal();
}
