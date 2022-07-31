package xyz.brassgoggledcoders.dailyresources.blockentity;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesBlocks;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesResources;
import xyz.brassgoggledcoders.dailyresources.menu.ResourceSelectorMenu;
import xyz.brassgoggledcoders.dailyresources.resource.Resource;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class ResourceStorageBlockEntity extends BlockEntity implements MenuProvider {
    private final Supplier<ResourceStorageOpenersCounter> containerOpenersCounter;
    private UUID uniqueId;
    private Component customName;

    public ResourceStorageBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
        this.containerOpenersCounter = Suppliers.memoize(() -> new ResourceStorageOpenersCounter(Objects.requireNonNull(this.getUniqueId())));
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.empty();
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

    public void setCustomName(Component textComponent) {
        this.customName = textComponent;
    }

    @Override
    @NotNull
    public Component getDisplayName() {
        return Objects.requireNonNullElseGet(this.customName, () -> this.getBlockState()
                .getBlock()
                .getName());
    }

    @Nullable
    @Override
    @ParametersAreNonnullByDefault
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        this.startOpen(pPlayer);
            /* TODO Handle post selection
            return new ResourceStorageMenu(
                    DailyResourcesBlocks.STORAGE_MENU.get(),
                    pContainerId,
                    pInventory,
                    this.getUniqueId(),
                    this::stillValid,
                    this::stopOpen
            );
             */
        return new ResourceSelectorMenu(
                DailyResourcesBlocks.SELECTOR_MENU.get(),
                pContainerId,
                pInventory,
                this::stillValid,
                this::stopOpen,
                DailyResources.RESOURCE_GROUP_MANAGER.getEntry(DailyResources.rl("planks"))
                        .map(resourceGroup -> resourceGroup.getChoicesFor(DailyResourcesResources.ITEMSTACK.get())
                                .entries()
                                .stream()
                                .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                                .toList()
                        )
                        .orElseGet(Collections::emptyList)
        );
    }

    public boolean stillValid(Player pPlayer) {
        if (this.getLevel() == null || this.getLevel().getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return 64.0D > pPlayer.distanceToSqr(
                    (double) this.worldPosition.getX() + 0.5D,
                    (double) this.worldPosition.getY() + 0.5D,
                    (double) this.worldPosition.getZ() + 0.5D
            );
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
    }

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains("UniqueId")) {
            this.uniqueId = pTag.getUUID("UniqueId");
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putUUID("UniqueId", this.getUniqueId());
    }

    public UUID getUniqueId() {
        if (this.uniqueId == null) {
            this.uniqueId = UUID.randomUUID();
        }
        return uniqueId;
    }

    public int calculateComparator() {
        //TODO CALC
        return 0;
    }

    public void openMenu(Player pPlayer) {
        if (pPlayer instanceof ServerPlayer serverPlayer) {
            //TODO HANDLE POST SELECTION
            NetworkHooks.openGui(
                    serverPlayer,
                    this,
                    friendlyByteBuf -> friendlyByteBuf.writeCollection(
                            DailyResources.RESOURCE_GROUP_MANAGER.getEntry(DailyResources.rl("planks"))
                                    .map(resourceGroup -> resourceGroup.getChoicesFor(DailyResourcesResources.ITEMSTACK.get())
                                            .entries()
                                            .stream()
                                            .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                                            .toList()
                                    )
                                    .orElseGet(Collections::emptyList),
                            (listByteBuf, pair) -> {
                                listByteBuf.writeWithCodec(Resource.CODEC.get(), pair.getFirst());
                                listByteBuf.writeItem(pair.getSecond());
                            }
                    )
            );
        }
    }
}