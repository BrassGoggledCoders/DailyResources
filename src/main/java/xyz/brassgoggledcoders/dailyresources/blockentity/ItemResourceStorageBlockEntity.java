package xyz.brassgoggledcoders.dailyresources.blockentity;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.capability.ItemHandlerWrapper;
import xyz.brassgoggledcoders.dailyresources.capability.ResourceStorage;
import xyz.brassgoggledcoders.dailyresources.capability.ResourceStorageStorage;
import xyz.brassgoggledcoders.dailyresources.codec.Codecs;
import xyz.brassgoggledcoders.dailyresources.menu.BasicMenuProvider;
import xyz.brassgoggledcoders.dailyresources.menu.ResourceSelectorMenu;
import xyz.brassgoggledcoders.dailyresources.resource.Choice;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceGroup;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceStorageSelection;
import xyz.brassgoggledcoders.dailyresources.resource.item.ItemStackResourceItemHandler;
import xyz.brassgoggledcoders.dailyresources.resource.item.ItemStackResourceStorage;
import xyz.brassgoggledcoders.dailyresources.screen.ResourceScreenType;
import xyz.brassgoggledcoders.dailyresources.screen.ResourceSelectorScreen;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class ItemResourceStorageBlockEntity extends ResourceStorageBlockEntity {
    private final Supplier<ResourceStorageOpenersCounter> containerOpenersCounter;
    private LazyOptional<IItemHandler> externalHandler;
    private final LazyOptional<IItemHandler> wrapperHandler;

    public ItemResourceStorageBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
        this.containerOpenersCounter = Suppliers.memoize(() -> new ResourceStorageOpenersCounter(Objects.requireNonNull(this.getUniqueId())));
        this.wrapperHandler = LazyOptional.of(() -> new ItemHandlerWrapper(this::getExternalHandler, 27));
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

    private Optional<IItemHandler> getHandlerOpt() {
        return this.getResourceStorageStorage()
                .resolve()
                .flatMap(storage -> storage.getCapability(this.getUniqueId(), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                        .resolve()
                );
    }

    public IItemHandler getHandler() {
        return this.wrapperHandler.orElseThrow(() -> new IllegalStateException("Found No Wrapper"));
    }

    private LazyOptional<IItemHandler> getExternalHandler() {
        if (this.externalHandler == null) {
            if (this.getResourceGroups() == null || this.getResourceGroups().isEmpty()) {
                this.externalHandler = LazyOptional.empty();
            } else {
                this.externalHandler = this.getHandlerOpt()
                        .map(handler -> LazyOptional.of(() -> handler))
                        .orElse(LazyOptional.empty());
            }
        }
        return this.externalHandler;
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

    public boolean onConfirmed(UUID id, ResourceGroup resourceGroup, Choice<ItemStack> choice, UUID owner) {
        Optional<ResourceLocation> resourceGroupId = DailyResources.RESOURCE_GROUP_MANAGER.getId(resourceGroup);
        if (this.externalHandler != null) {
            this.externalHandler.invalidate();
            this.externalHandler = null;
        }

        this.clearCache();

        return resourceGroupId.isPresent() && this.getResourceStorageStorage()
                .map(resourceStorageStorage -> {
                    ResourceStorage resourceStorage = resourceStorageStorage.getOrCreateResourceStorage(
                            this.getUniqueId(),
                            () -> new ItemStackResourceStorage(ItemStackResourceItemHandler.create(27))
                    );

                    return resourceStorage.addSelection(new ResourceStorageSelection<>(
                            id,
                            resourceGroupId.get(),
                            choice,
                            owner
                    ));
                })
                .orElse(false);
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

    public void openMenu(Player pPlayer, @Nullable ResourceScreenType resourceScreenType) {
        this.startOpen(pPlayer);
        if (pPlayer instanceof ServerPlayer serverPlayer) {
            if (pPlayer.containerMenu instanceof ResourceSelectorMenu<?> menu) {
                menu.confirmChoices(pPlayer);
            }
            List<Pair<UUID, ResourceGroup>> choices = this.getCachedGroupsForChoices();
            if (resourceScreenType == null) {
                resourceScreenType = choices.isEmpty() ? ResourceScreenType.ITEM_STORAGE : ResourceScreenType.ITEM_SELECTOR;
            }
            ResourceScreenType finalResourceScreenType = resourceScreenType;
            NetworkHooks.openGui(
                    serverPlayer,
                    new BasicMenuProvider<>(
                            resourceScreenType.getDisplayName(),
                            resourceScreenType.getMenuFunction(this)
                    ),
                    friendlyByteBuf -> {
                        if (finalResourceScreenType == ResourceScreenType.ITEM_SELECTOR) {
                            friendlyByteBuf.writeCollection(
                                    choices,
                                    (listByteBuf, pair) -> {
                                        listByteBuf.writeUUID(pair.getFirst());
                                        listByteBuf.writeWithCodec(ResourceGroup.CODEC.get(), pair.getSecond());
                                    }
                            );
                        }
                        friendlyByteBuf.writeCollection(
                                finalResourceScreenType.getTabs(
                                        this.getBlockState().getBlock(),
                                        this.getCachedGroupsForChoices()
                                ),
                                (listByteBuf, tab) -> {
                                    listByteBuf.writeItem(tab.icon());
                                    listByteBuf.writeCollection(
                                            tab.components(),
                                            (subListByteBuffer, component) -> subListByteBuffer.writeWithCodec(Codecs.COMPONENT, component)
                                    );
                                    listByteBuf.writeEnum(tab.marker());
                                }
                        );
                    }
            );
        }
    }
}
