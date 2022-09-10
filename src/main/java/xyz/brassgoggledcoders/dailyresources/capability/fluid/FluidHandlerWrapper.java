package xyz.brassgoggledcoders.dailyresources.capability.fluid;

import com.google.common.base.Suppliers;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class FluidHandlerWrapper implements IFluidHandlerModifiable {
    private final Supplier<LazyOptional<IFluidHandler>> wrappedHandler;
    private final int defaultTanks;
    private final int defaultCapacity;

    public FluidHandlerWrapper(Supplier<LazyOptional<IFluidHandler>> wrappedHandler, int defaultTanks, int defaultCapacity) {
        this.wrappedHandler = Suppliers.memoize(wrappedHandler::get);
        this.defaultTanks = defaultTanks;
        this.defaultCapacity = defaultCapacity;
    }

    @Override
    public int getTanks() {
        return this.wrappedHandler.get()
                .map(IFluidHandler::getTanks)
                .orElse(defaultTanks);
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return this.wrappedHandler.get()
                .map(fluidHandler -> fluidHandler.getFluidInTank(tank))
                .orElse(FluidStack.EMPTY);
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.wrappedHandler.get()
                .map(fluidHandler -> fluidHandler.getTankCapacity(tank))
                .orElse(defaultCapacity);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return this.wrappedHandler.get()
                .map(fluidHandler -> fluidHandler.isFluidValid(tank, stack))
                .orElse(false);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return this.wrappedHandler.get()
                .map(fluidHandler -> fluidHandler.drain(resource, action))
                .orElse(FluidStack.EMPTY);
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return this.wrappedHandler.get()
                .map(fluidHandler -> fluidHandler.drain(maxDrain, action))
                .orElse(FluidStack.EMPTY);
    }

    @Override
    public void setFluidInTank(int tank, @NotNull FluidStack stack) {
        wrappedHandler.get()
                .ifPresent(wrapped -> {
                    if (wrapped instanceof IFluidHandlerModifiable modifiable) {
                        modifiable.setFluidInTank(tank, stack);
                    }
                });
    }

    @Override
    public int getSignal() {
        int capacity = 0;
        int filled = 0;

        for (int i = 0; i < this.getTanks(); i++) {
            capacity += this.getTankCapacity(i);
            filled += this.getFluidInTank(i).getAmount();
        }

        return capacity > 0 ? filled / capacity : 0;
    }
}
