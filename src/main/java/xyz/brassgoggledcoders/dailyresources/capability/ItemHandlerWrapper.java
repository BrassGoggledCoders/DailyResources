package xyz.brassgoggledcoders.dailyresources.capability;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ItemHandlerWrapper implements IItemHandlerModifiable {
    private final Supplier<LazyOptional<IItemHandler>> wrappedReceiver;
    private final int defaultSlots;

    public ItemHandlerWrapper(Supplier<LazyOptional<IItemHandler>> wrappedReceiver, int defaultSlots) {
        this.wrappedReceiver = wrappedReceiver;
        this.defaultSlots = defaultSlots;
    }

    @Override
    public int getSlots() {
        return wrappedReceiver.get()
                .map(IItemHandler::getSlots)
                .orElse(this.defaultSlots);
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return wrappedReceiver.get()
                .map(wrapped -> wrapped.getStackInSlot(slot))
                .orElse(ItemStack.EMPTY);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return stack;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return wrappedReceiver.get()
                .map(wrapped -> wrapped.extractItem(slot, amount, simulate))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public int getSlotLimit(int slot) {
        return wrappedReceiver.get()
                .map(wrapped -> wrapped.getSlotLimit(slot))
                .orElse(0);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return wrappedReceiver.get()
                .map(wrapped -> wrapped.isItemValid(slot, stack))
                .orElse(false);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        wrappedReceiver.get()
                .ifPresent(wrapped -> {
                    if (wrapped instanceof IItemHandlerModifiable modifiable) {
                        modifiable.setStackInSlot(slot, stack);
                    }
                });
    }
}
