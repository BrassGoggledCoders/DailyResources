package xyz.brassgoggledcoders.dailyresources.resource.fluid;

import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

public class FluidStackResourceFluidHandler implements IFluidHandler {
    private final IFluidTank[] fluidTanks;

    public FluidStackResourceFluidHandler(int tanks) {
        this.fluidTanks = new IFluidTank[tanks];
        for (int i = 0; i < tanks; i++) {
            this.fluidTanks[i] = new FluidTank(FluidAttributes.BUCKET_VOLUME * 16);
        }
    }

    @Override
    public int getTanks() {
        return fluidTanks.length;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        if (tank >= 0 && tank < fluidTanks.length) {
            return fluidTanks[tank].getFluid();
        }
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        if (tank >= 0 && tank < fluidTanks.length) {
            return fluidTanks[tank].getCapacity();
        }
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        if (tank >= 0 && tank < fluidTanks.length) {
            return fluidTanks[tank].isFluidValid(stack);
        }
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        int filled = 0;
        int tank = 0;
        while (tank < fluidTanks.length && filled < resource.getAmount()) {
            FluidStack filling = resource;
            if (filled != 0) {
                filling = resource.copy();
                filling.setAmount(resource.getAmount() - filled);
            }
            filled += fluidTanks[tank].fill(filling, action);
            tank++;
        }
        return filled;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        FluidStack fluidStack = FluidStack.EMPTY;
        int tank = 0;
        while (tank < fluidTanks.length && fluidStack.getAmount() < resource.getAmount()) {
            if (fluidStack.isEmpty()) {
                fluidStack = fluidTanks[tank].drain(resource, action);
            } else if (resource.isFluidEqual(fluidTanks[tank].getFluid())) {
                fluidStack.grow(fluidTanks[tank].drain(resource.getAmount() - fluidStack.getAmount(), action).getAmount());
            }
            tank++;
        }
        return fluidStack;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack fluidStack = FluidStack.EMPTY;
        int tank = 0;
        while (tank++ < fluidTanks.length && fluidStack.getAmount() < maxDrain) {
            if (fluidStack.isEmpty()) {
                fluidStack = fluidTanks[tank].drain(maxDrain, action);
            } else if (fluidStack.isFluidEqual(fluidTanks[tank].getFluid())) {
                fluidStack.grow(fluidTanks[tank].drain(maxDrain - fluidStack.getAmount(), action).getAmount());
            }
        }
        return fluidStack;
    }
}
