package xyz.brassgoggledcoders.dailyresources.blockentity;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
import xyz.brassgoggledcoders.dailyresources.capability.ResourceStorage;
import xyz.brassgoggledcoders.dailyresources.capability.ResourceStorageStorage;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesBlocks;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesResources;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesTriggers;
import xyz.brassgoggledcoders.dailyresources.menu.Choice;
import xyz.brassgoggledcoders.dailyresources.menu.ResourceSelectorMenu;
import xyz.brassgoggledcoders.dailyresources.menu.ResourceStorageMenu;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceGroup;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceStorageSelection;
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
    private Map<UUID, ResourceLocation> resourceGroups;
    private Trigger trigger;
    private Trigger nbtTrigger;
    private LazyOptional<ResourceStorageStorage> storageLazyOptional;

    public ResourceStorageBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
        this.containerOpenersCounter = Suppliers.memoize(() -> new ResourceStorageOpenersCounter(Objects.requireNonNull(this.getUniqueId())));
    }

    @Nullable
    public Trigger getTrigger() {
        if (this.nbtTrigger != null) {
            return this.nbtTrigger;
        }
        if (this.trigger == null && this.getLevel() instanceof ServerLevel) {
            this.trigger = Optional.ofNullable(this.resourceGroups)
                    .stream()
                    .parallel()
                    .flatMap(map -> map.values().stream())
                    .distinct()
                    .map(DailyResources.RESOURCE_GROUP_MANAGER::getEntry)
                    .flatMap(Optional::stream)
                    .map(ResourceGroup::trigger)
                    .reduce(Trigger::merge)
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
            return new ResourceSelectorMenu<>(
                    DailyResourcesBlocks.ITEM_SELECTOR_MENU.get(),
                    pContainerId,
                    pInventory,
                    this::stillValid,
                    this::stopOpen,
                    this::onConfirmed,
                    this.getGroupsForChoices(),
                    DailyResourcesResources.ITEMSTACK.get()
            );
        }
    }

    private List<Pair<UUID, ResourceGroup>> getGroupsForChoices() {
        return Optional.ofNullable(this.resourceGroups)
                .stream()
                .flatMap(map -> map.entrySet().stream())
                .map(entry -> DailyResources.RESOURCE_GROUP_MANAGER.getEntry(entry.getValue())
                        .map(group -> Pair.of(entry.getKey(), group))
                )
                .flatMap(Optional::stream)
                .toList();
    }

    private boolean onConfirmed(UUID id, ResourceGroup resourceGroup, Choice<ItemStack> choice, UUID owner) {
        Optional<ResourceLocation> resourceGroupId = DailyResources.RESOURCE_GROUP_MANAGER.getId(resourceGroup);

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
        if (pTag.contains("Trigger")) {
            this.nbtTrigger = DailyResourcesTriggers.REGISTRY.get()
                    .getValue(new ResourceLocation(pTag.getString("Trigger")));
        }
        if (pTag.contains("ResourceGroup")) {
            this.resourceGroups = new HashMap<>();
            this.resourceGroups.put(UUID.randomUUID(), new ResourceLocation(pTag.getString("ResourceGroup")));
        } else if (pTag.contains("ResourceGroups", Tag.TAG_LIST)) {
            this.resourceGroups = new HashMap<>();
            ListTag listTag = pTag.getList("ResourceGroups", Tag.TAG_STRING);
            for (int i = 0; i < listTag.size(); i++) {
                this.resourceGroups.put(UUID.randomUUID(), new ResourceLocation(listTag.getString(i)));
            }
        } else if (pTag.contains("ResourceGroups", Tag.TAG_COMPOUND)) {
            this.resourceGroups = new HashMap<>();
            CompoundTag tag = pTag.getCompound("ResourceGroups");
            for (String key : tag.getAllKeys()) {
                this.resourceGroups.put(UUID.fromString(key), new ResourceLocation(tag.getString(key)));
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putUUID("UniqueId", this.getUniqueId());
        if (this.resourceGroups != null) {
            CompoundTag compoundTag = new CompoundTag();
            for (Map.Entry<UUID, ResourceLocation> entry : this.resourceGroups.entrySet()) {
                compoundTag.putString(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        if (this.nbtTrigger != null) {
            pTag.putString("Trigger", Objects.requireNonNull(this.nbtTrigger.getRegistryName()).toString());
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
                                    this.getGroupsForChoices(),
                                    (listByteBuf, pair) -> {
                                        listByteBuf.writeUUID(pair.getFirst());
                                        listByteBuf.writeWithCodec(ResourceGroup.CODEC.get(), pair.getSecond());
                                    }
                            );
                        }
                    }
            );
        }
    }
}
