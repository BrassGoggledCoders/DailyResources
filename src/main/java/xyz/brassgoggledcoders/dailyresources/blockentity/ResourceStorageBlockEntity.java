package xyz.brassgoggledcoders.dailyresources.blockentity;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.capability.ResourceStorageStorage;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesBlocks;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesResources;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesTriggers;
import xyz.brassgoggledcoders.dailyresources.menu.ResourceSelectorMenu;
import xyz.brassgoggledcoders.dailyresources.menu.ResourceStorageMenu;
import xyz.brassgoggledcoders.dailyresources.resource.Resource;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceGroup;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceStorageInfo;
import xyz.brassgoggledcoders.dailyresources.resource.item.ItemStackResourceItemHandler;
import xyz.brassgoggledcoders.dailyresources.resource.item.ItemStackResourceStorage;
import xyz.brassgoggledcoders.dailyresources.trigger.Trigger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Supplier;

public class ResourceStorageBlockEntity extends BlockEntity implements MenuProvider {
    public final static ModelProperty<Trigger> TRIGGER_PROPERTY = new ModelProperty<>();
    private final Supplier<ResourceStorageOpenersCounter> containerOpenersCounter;
    private UUID uniqueId;
    private Component customName;
    private ResourceLocation resourceGroup;
    private Trigger trigger;
    private LazyOptional<ResourceStorageStorage> storageLazyOptional;

    public ResourceStorageBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
        this.containerOpenersCounter = Suppliers.memoize(() -> new ResourceStorageOpenersCounter(Objects.requireNonNull(this.getUniqueId())));
    }

    @Nullable
    public Trigger getTrigger() {
        if (this.trigger == null && this.getLevel() instanceof ServerLevel) {
            this.trigger = Optional.ofNullable(this.resourceGroup)
                    .flatMap(DailyResources.RESOURCE_GROUP_MANAGER::getEntry)
                    .map(ResourceGroup::trigger)
                    .orElseGet(DailyResourcesTriggers.NONE);
        }
        return this.trigger;
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

    private LazyOptional<ResourceStorageStorage> getResourceStorageStorage() {
        if (this.getLevel() instanceof ServerLevel serverLevel) {
            if (serverLevel.dimension() == Level.OVERWORLD) {
                storageLazyOptional = serverLevel.getCapability(ResourceStorageStorage.CAP);
            } else {
                Level overWorld = serverLevel.getServer()
                        .getLevel(Level.OVERWORLD);

                if (overWorld != null) {
                    storageLazyOptional = overWorld.getCapability(ResourceStorageStorage.CAP);
                }
            }
        }
        if (storageLazyOptional == null) {
            this.storageLazyOptional = LazyOptional.empty();
        } else {
            this.storageLazyOptional.addListener(this::refreshStorageStorage);
        }
        return storageLazyOptional;
    }

    private void refreshStorageStorage(LazyOptional<ResourceStorageStorage> lazyOptional) {
        this.storageLazyOptional = null;
    }

    private boolean hasGroupSelection() {
        return this.getResourceStorageStorage()
                .filter(storage -> storage.hasResourceSource(this.getUniqueId()))
                .isPresent();
    }

    private IItemHandler getHandler() {
        return this.getResourceStorageStorage()
                .resolve()
                .flatMap(storage -> storage.getCapability(this.getUniqueId(), CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                        .resolve()
                )
                .orElseGet(() -> new ItemStackHandler(27));
    }

    @Nullable
    @Override
    @ParametersAreNonnullByDefault
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        this.startOpen(pPlayer);
        if (this.hasGroupSelection()) {
            return new ResourceStorageMenu(
                    DailyResourcesBlocks.STORAGE_MENU.get(),
                    pContainerId,
                    pInventory,
                    this.getHandler(),
                    this.getUniqueId(),
                    this::stillValid,
                    this::stopOpen
            );
        } else {
            return new ResourceSelectorMenu(
                    DailyResourcesBlocks.SELECTOR_MENU.get(),
                    pContainerId,
                    pInventory,
                    this::stillValid,
                    this::stopOpen,
                    this::onConfirmed,
                    this.getChoices()
            );
        }
    }

    private List<Pair<Resource, ItemStack>> getChoices() {
        return Optional.ofNullable(this.resourceGroup)
                .flatMap(DailyResources.RESOURCE_GROUP_MANAGER::getEntry)
                .map(resourceGroup -> resourceGroup.getChoicesFor(DailyResourcesResources.ITEMSTACK.get())
                        .entries()
                        .stream()
                        .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                        .toList()
                )
                .orElseGet(Collections::emptyList);
    }

    private Void onConfirmed(Resource resource, ItemStack itemStack, UUID owner) {
        this.getResourceStorageStorage()
                .filter(resourceStorageStorage -> !resourceStorageStorage.hasResourceSource(this.getUniqueId()))
                .ifPresent(resourceStorageStorage -> resourceStorageStorage.createResourceStorage(
                        this.getUniqueId(),
                        new ItemStackResourceStorage(
                                new ResourceStorageInfo(
                                        this.resourceGroup,
                                        resource,
                                        itemStack,
                                        owner
                                ),
                                new ItemStackResourceItemHandler(
                                        NonNullList.withSize(27, ItemStack.EMPTY)
                                )
                        )
                ));
        return null;
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
        if (pTag.contains("ResourceGroup")) {
            this.resourceGroup = new ResourceLocation(pTag.getString("ResourceGroup"));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putUUID("UniqueId", this.getUniqueId());
        if (this.resourceGroup != null) {
            pTag.putString("ResourceGroup", this.resourceGroup.toString());
        }
    }

    @Override
    @NotNull
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        Optional.ofNullable(this.getTrigger())
                .map(Trigger::getRegistryName)
                .map(ResourceLocation::toString)
                .ifPresent(triggerName -> tag.putString("Trigger", triggerName));
        return tag;
    }

    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            this.handleUpdateTag(tag);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        if (tag.contains("Trigger")) {
            this.trigger = DailyResourcesTriggers.REGISTRY.get().getValue(new ResourceLocation(tag.getString("Trigger")));
            requestModelDataUpdate();
            if (level != null) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    public UUID getUniqueId() {
        if (this.uniqueId == null) {
            this.uniqueId = UUID.randomUUID();
        }
        return uniqueId;
    }

    @NotNull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder()
                .withInitial(TRIGGER_PROPERTY, this.getTrigger())
                .build();
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
                    friendlyByteBuf -> {
                        if (!this.hasGroupSelection()) {
                            friendlyByteBuf.writeCollection(
                                    this.getChoices(),
                                    (listByteBuf, pair) -> {
                                        listByteBuf.writeWithCodec(Resource.CODEC.get(), pair.getFirst());
                                        listByteBuf.writeItem(pair.getSecond());
                                    }

                            );
                        }
                    }
            );
        }
    }
}
