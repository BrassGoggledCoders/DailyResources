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
import net.minecraft.world.Containers;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
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
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.capability.ItemHandlerWrapper;
import xyz.brassgoggledcoders.dailyresources.capability.ResourceStorage;
import xyz.brassgoggledcoders.dailyresources.capability.ResourceStorageStorage;
import xyz.brassgoggledcoders.dailyresources.codec.Codecs;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesTriggers;
import xyz.brassgoggledcoders.dailyresources.menu.BasicMenuProvider;
import xyz.brassgoggledcoders.dailyresources.resource.Choice;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceGroup;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceStorageSelection;
import xyz.brassgoggledcoders.dailyresources.resource.item.ItemStackResourceItemHandler;
import xyz.brassgoggledcoders.dailyresources.resource.item.ItemStackResourceStorage;
import xyz.brassgoggledcoders.dailyresources.screen.ResourceScreenType;
import xyz.brassgoggledcoders.dailyresources.trigger.Trigger;
import xyz.brassgoggledcoders.dailyresources.util.CachedValue;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ItemResourceStorageBlockEntity extends BlockEntity implements Nameable {
    public final static ModelProperty<Trigger> TRIGGER_PROPERTY = new ModelProperty<>();
    private final Supplier<ResourceStorageOpenersCounter> containerOpenersCounter;
    private UUID uniqueId;
    private Component customName;
    private Map<UUID, ResourceLocation> resourceGroups;
    private Trigger trigger;
    private Trigger nbtTrigger;
    private LazyOptional<ResourceStorageStorage> storageLazyOptional;
    private LazyOptional<IItemHandler> externalHandler;
    private final LazyOptional<IItemHandler> wrapperHandler;

    private final CachedValue<List<Pair<UUID, ResourceGroup>>> cachedGroups;

    public ItemResourceStorageBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
        this.containerOpenersCounter = Suppliers.memoize(() -> new ResourceStorageOpenersCounter(Objects.requireNonNull(this.getUniqueId())));
        this.wrapperHandler = LazyOptional.of(() -> new ItemHandlerWrapper(this::getExternalHandler, 27));
        this.cachedGroups = new CachedValue<>(
                1,
                this::getGroupsForChoices,
                () -> this.getLevel() == null ? 0 : this.getLevel().getGameTime()
        );
    }

    @Nullable
    public Trigger getTrigger() {
        if (this.nbtTrigger != null) {
            return this.nbtTrigger;
        }
        if (this.trigger == null && this.getLevel() instanceof ServerLevel) {
            this.trigger = Optional.ofNullable(this.resourceGroups)
                    .map(Map::values)
                    .stream()
                    .flatMap(Collection::stream)
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

    public void setCustomName(Component textComponent) {
        this.customName = textComponent;
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
            if (this.resourceGroups == null || this.resourceGroups.isEmpty()) {
                this.externalHandler = LazyOptional.empty();
            } else {
                this.externalHandler = this.getHandlerOpt()
                        .map(handler -> LazyOptional.of(() -> handler))
                        .orElse(LazyOptional.empty());
            }
        }
        return this.externalHandler;
    }

    private List<Pair<UUID, ResourceGroup>> getGroupsForChoices() {
        Predicate<UUID> predicate = this.getResourceStorageStorage()
                .resolve()
                .map(storage -> storage.getResourceStorage(this.getUniqueId()))
                .map(storage -> Predicate.not(storage::hasSelection))
                .orElseGet(() -> entry -> true);
        return Optional.ofNullable(this.resourceGroups)
                .stream()
                .flatMap(map -> map.entrySet().stream())
                .filter(entry -> predicate.test(entry.getKey()))
                .map(entry -> DailyResources.RESOURCE_GROUP_MANAGER.getEntry(entry.getValue())
                        .map(group -> Pair.of(entry.getKey(), group))
                )
                .flatMap(Optional::stream)
                .toList();
    }

    public List<Pair<UUID, ResourceGroup>> getCachedGroupsForChoices() {
        return this.cachedGroups.get();
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

    @Override
    @NotNull
    public Component getName() {
        if (this.hasCustomName()) {
            return this.customName;
        } else {
            return this.getBlockState()
                    .getBlock()
                    .getName();
        }
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return this.customName;
    }

    public ContainerLevelAccess createLevelAccess() {
        if (this.getLevel() != null) {
            return ContainerLevelAccess.create(this.getLevel(), this.getBlockPos());
        } else {
            return ContainerLevelAccess.NULL;
        }
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

    @Override
    public void load(@NotNull CompoundTag pTag) {
        super.load(pTag);
        this.trigger = null;
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
            CompoundTag resourceGroupTag = new CompoundTag();
            for (Map.Entry<UUID, ResourceLocation> entry : this.resourceGroups.entrySet()) {
                resourceGroupTag.putString(entry.getKey().toString(), entry.getValue().toString());
            }
            pTag.put("ResourceGroups", resourceGroupTag);
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
        return this.wrapperHandler.map(ItemHandlerHelper::calcRedstoneFromInventory)
                .orElse(0);
    }

    public void openMenu(Player pPlayer, @Nullable ResourceScreenType resourceScreenType) {
        this.startOpen(pPlayer);
        if (pPlayer instanceof ServerPlayer serverPlayer) {
            List<Pair<UUID, ResourceGroup>> choices = this.getGroupsForChoices();
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