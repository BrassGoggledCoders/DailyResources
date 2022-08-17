package xyz.brassgoggledcoders.dailyresources.resource.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public record ItemStackResourceItemHandler(
        NonNullList<ItemStack> itemStacks
) implements IItemHandler {
    public static final Codec<ItemStackResourceItemHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(ItemStack.CODEC).fieldOf("itemStacks").forGetter(ItemStackResourceItemHandler::getItemStackList)
    ).apply(instance, ItemStackResourceItemHandler::create));

    @Override
    public int getSlots() {
        return this.itemStacks().size();
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return this.itemStacks().get(slot);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (!isItemValid(slot, stack)) {
            return stack;
        }

        validateSlotIndex(slot);

        ItemStack existing = this.itemStacks().get(slot);

        int limit = getStackLimit(slot, stack);

        if (!existing.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) {
                return stack;
            }

            limit -= existing.getCount();
        }

        if (limit <= 0) {
            return stack;
        }

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                this.itemStacks().set(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return ItemStack.EMPTY;
        }

        validateSlotIndex(slot);

        ItemStack existing = this.itemStacks().get(slot);

        if (existing.isEmpty())
            return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                this.itemStacks().set(slot, ItemStack.EMPTY);
                return existing;
            } else {
                return existing.copy();
            }
        } else {
            if (!simulate) {
                this.itemStacks().set(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
            }

            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    private int getStackLimit(int slot, @Nonnull ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return true;
    }


    public List<ItemStack> getItemStackList() {
        return new ArrayList<>(this.itemStacks);
    }

    private void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= itemStacks().size())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + itemStacks().size() + ")");
    }

    public static ItemStackResourceItemHandler create(List<ItemStack> list) {
        NonNullList<ItemStack> itemStackList = NonNullList.create();
        itemStackList.addAll(list);
        return new ItemStackResourceItemHandler(itemStackList);
    }

    public static ItemStackResourceItemHandler create(int size) {
        return new ItemStackResourceItemHandler(
                NonNullList.withSize(size, ItemStack.EMPTY)
        );
    }
}
