package xyz.brassgoggledcoders.dailyresources.blockentity;

import com.google.common.base.Suppliers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.dailyresources.capability.ItemHandlerWrapper;
import xyz.brassgoggledcoders.dailyresources.capability.ResourceStorage;
import xyz.brassgoggledcoders.dailyresources.capability.ResourceStorageStorage;
import xyz.brassgoggledcoders.dailyresources.resource.Choice;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceGroup;
import xyz.brassgoggledcoders.dailyresources.resource.item.ItemStackResourceItemHandler;
import xyz.brassgoggledcoders.dailyresources.resource.item.ItemStackResourceStorage;
import xyz.brassgoggledcoders.dailyresources.screen.ResourceScreenType;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class ItemResourceStorageBlockEntity extends ResourceStorageBlockEntity<ItemStack> {
    private final Supplier<ResourceStorageOpenersCounter> containerOpenersCounter;
    private LazyOptional<IItemHandler> externalHandler;
    private LazyOptional<IItemHandler> wrapperHandler;

    public ItemResourceStorageBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
        this.containerOpenersCounter = Suppliers.memoize(() -> new ResourceStorageOpenersCounter(Objects.requireNonNull(this.getUniqueId())));
        this.wrapperHandler = LazyOptional.of(() -> new ItemHandlerWrapper(this::getStorageItemHandler, 27));
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return this.wrapperHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    public void startOpen(Player pPlayer) {
        if (!this.isRemoved() && !pPlayer.isSpectator() && this.getLevel() != null) {
            this.containerOpenersCounter.get().incrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public void stopOpen(Player pPlayer) {
        if (!this.isRemoved() && !pPlayer.isSpectator() && this.getLevel() != null) {
            this.containerOpenersCounter.get().decrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public void recheckOpen() {
        if (!this.isRemoved() && this.getLevel() != null) {
            this.containerOpenersCounter.get().recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    protected void refreshStorageStorage(LazyOptional<ResourceStorageStorage> lazyOptional) {
        super.refreshStorageStorage(lazyOptional);
        if (this.externalHandler != null && this.externalHandler.isPresent()) {
            this.externalHandler.invalidate();
            this.externalHandler = null;
        }
    }

    @Override
    protected ResourceScreenType getDefaultScreenType(boolean hasChoices) {
        return hasChoices ? ResourceScreenType.ITEM_SELECTOR : ResourceScreenType.ITEM_STORAGE;
    }

    protected void refreshStorageItemHandler(LazyOptional<IItemHandler> lazyOptional) {
        wrapperHandler.invalidate();
        wrapperHandler = LazyOptional.of(() -> new ItemHandlerWrapper(this::getStorageItemHandler, 27));
    }

    public IItemHandler getHandler() {
        return this.wrapperHandler.orElseThrow(() -> new IllegalStateException("Found No Wrapper"));
    }

    private LazyOptional<IItemHandler> getStorageItemHandler() {
        LazyOptional<IItemHandler> handlerLazyOptional = this.getResourceStorageStorage()
                .<LazyOptional<IItemHandler>>map(storageStorage -> {
                    ResourceStorage resourceStorage = storageStorage.getResourceStorage(this.getUniqueId());
                    if (resourceStorage != null) {
                        return resourceStorage.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                    } else {
                        return LazyOptional.empty();
                    }
                })
                .orElse(LazyOptional.empty());
        handlerLazyOptional.addListener(this::refreshStorageItemHandler);
        return handlerLazyOptional;
    }

    public void remove() {
        this.getResourceStorageStorage()
                .ifPresent(resourceStorageStorage -> {
                    ResourceStorage resourceStorage = resourceStorageStorage.deleteResourceStorage(this.getUniqueId());
                    if (resourceStorage != null) {
                        resourceStorage.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                                .ifPresent(inventory -> {
                                    BlockPos pos = this.getBlockPos();
                                    dropContents(this.getLevel(), pos.getX(), pos.getY(), pos.getZ(), inventory);
                                });
                    }
                });
    }

    private static void dropContents(Level pLevel, double pX, double pY, double pZ, IItemHandler pInventory) {
        for (int i = 0; i < pInventory.getSlots(); ++i) {
            Containers.dropItemStack(pLevel, pX, pY, pZ, pInventory.getStackInSlot(i));
        }
    }

    @Override
    protected ResourceStorage createDefaultResourceStorage() {
        return new ItemStackResourceStorage(ItemStackResourceItemHandler.create(27));
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.wrapperHandler.invalidate();
    }

    public int calculateComparator() {
        return this.wrapperHandler.map(ItemHandlerHelper::calcRedstoneFromInventory)
                .orElse(0);
    }

    @Override
    public void openMenu(Player pPlayer, @Nullable ResourceScreenType resourceScreenType) {
        this.startOpen(pPlayer);
        super.openMenu(pPlayer, resourceScreenType);
    }

    @Override
    public boolean onConfirmed(UUID id, ResourceGroup resourceGroup, Choice<ItemStack> choice, UUID owner) {
        this.wrapperHandler.invalidate();
        this.wrapperHandler = LazyOptional.of(() -> new ItemHandlerWrapper(this::getStorageItemHandler, 27));
        return super.onConfirmed(id, resourceGroup, choice, owner);
    }
}
