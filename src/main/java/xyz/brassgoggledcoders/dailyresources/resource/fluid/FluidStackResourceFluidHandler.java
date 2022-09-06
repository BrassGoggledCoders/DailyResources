package xyz.brassgoggledcoders.dailyresources.resource.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FluidStackResourceFluidHandler implements IFluidHandler {

    public static final Codec<FluidTank> TANK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FluidStack.CODEC.fieldOf("fluid").forGetter(FluidTank::getFluid)
    ).apply(instance, fluidStack -> {
        FluidTank fluidTank = new FluidTank(FluidAttributes.BUCKET_VOLUME * 16);
        fluidTank.setFluid(fluidStack);
        return fluidTank;
    }));

    public static final Codec<FluidStackResourceFluidHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(TANK_CODEC)
                    .fieldOf("fluidTanks")
                    .forGetter(FluidStackResourceFluidHandler::getFluidTanks)
    ).apply(instance, FluidStackResourceFluidHandler::new));

    private final List<FluidTank> fluidTanks;

    public FluidStackResourceFluidHandler(int tanks) {
        this.fluidTanks = new ArrayList<>();
        for (int i = 0; i < tanks; i++) {
            this.fluidTanks.add(new FluidTank(FluidAttributes.BUCKET_VOLUME * 16));
        }
    }

    public FluidStackResourceFluidHandler(List<FluidTank> fluidTanks) {
        this.fluidTanks = fluidTanks;
    }

    @Override
    public int getTanks() {
        return fluidTanks.size();
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        if (tank >= 0 && tank < fluidTanks.size()) {
            return fluidTanks.get(tank).getFluid();
        }
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        if (tank >= 0 && tank < fluidTanks.size()) {
            return fluidTanks.get(tank).getCapacity();
        }
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        if (tank >= 0 && tank < fluidTanks.size()) {
            return fluidTanks.get(tank).isFluidValid(stack);
        }
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        int filled = 0;
        int tank = 0;
        while (tank < fluidTanks.size() && filled < resource.getAmount()) {
            FluidStack filling = resource;
            if (filled != 0) {
                filling = resource.copy();
                filling.setAmount(resource.getAmount() - filled);
            }
            filled += fluidTanks.get(tank).fill(filling, action);
            tank++;
        }
        return filled;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        FluidStack fluidStack = FluidStack.EMPTY;
        Iterator<FluidTank> tankIterator = fluidTanks.iterator();
        while (tankIterator.hasNext() && fluidStack.getAmount() < resource.getAmount()) {
            FluidTank nextTank = tankIterator.next();
            if (fluidStack.isEmpty()) {
                fluidStack = nextTank.drain(resource, action);
            } else if (resource.isFluidEqual(nextTank.getFluid())) {
                fluidStack.grow(nextTank.drain(resource.getAmount() - fluidStack.getAmount(), action).getAmount());
            }
        }
        return fluidStack;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack fluidStack = FluidStack.EMPTY;
        Iterator<FluidTank> tankIterator = fluidTanks.iterator();
        while (tankIterator.hasNext() && fluidStack.getAmount() < maxDrain) {
            FluidTank nextTank = tankIterator.next();
            if (fluidStack.isEmpty()) {
                fluidStack = nextTank.drain(maxDrain, action);
            } else if (fluidStack.isFluidEqual(nextTank.getFluid())) {
                fluidStack.grow(nextTank.drain(maxDrain - fluidStack.getAmount(), action).getAmount());
            }
        }
        return fluidStack;
    }

    public List<FluidTank> getFluidTanks() {
        return fluidTanks;
    }
}
