package xyz.brassgoggledcoders.dailyresources.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.dailyresources.codec.Codecs;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesBlocks;
import xyz.brassgoggledcoders.dailyresources.menu.slot.NoPlaceSlot;
import xyz.brassgoggledcoders.dailyresources.screen.ResourceScreenType;
import xyz.brassgoggledcoders.dailyresources.screen.Tab;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ItemResourceStorageMenu extends AbstractContainerMenu {
    private final UUID uniqueId;
    private final Consumer<Player> closeHandler;
    private final IItemHandler itemHandler;
    private final List<Tab<ResourceScreenType>> tabs;
    private final ContainerLevelAccess levelAccess;

    public ItemResourceStorageMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory inventory, IItemHandler storageInventory, UUID uniqueId,
                                   ContainerLevelAccess levelAccess, Consumer<Player> closeHandler, List<Tab<ResourceScreenType>> tabs) {
        super(pMenuType, pContainerId);
        this.uniqueId = uniqueId;
        this.itemHandler = storageInventory;
        this.levelAccess = levelAccess;
        this.closeHandler = closeHandler;
        this.tabs = tabs;
        int containerRows = storageInventory.getSlots() / 9;
        int i = (containerRows - 4) * 18;

        for (int j = 0; j < containerRows; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new NoPlaceSlot(storageInventory, k + j * 9, 8 + k * 18, 18 + j * 18));
            }
        }

        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(inventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + i));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(inventory, i1, 8 + i1 * 18, 161 + i));
        }
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return stillValid(levelAccess, pPlayer, DailyResourcesBlocks.BARREL.get());
    }

    @Override
    public void removed(@NotNull Player pPlayer) {
        super.removed(pPlayer);
        this.closeHandler.accept(pPlayer);
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public List<Tab<ResourceScreenType>> getTabs() {
        return this.tabs;
    }

    @Override
    public boolean clickMenuButton(@NotNull Player pPlayer, int pId) {
        if (pId == 0 && !this.getTabs().isEmpty()) {
            this.levelAccess.execute((level, blockPos) -> DailyResourcesBlocks.STORAGE_BLOCK_ENTITY.get(level, blockPos)
                    .ifPresent(storage -> storage.openMenu(pPlayer, ResourceScreenType.ITEM_SELECTOR))
            );
            return true;
        }

        return false;
    }

    @NotNull
    public static ItemResourceStorageMenu create(MenuType<ItemResourceStorageMenu> menuType, int id, Inventory inventory,
                                                 @Nullable FriendlyByteBuf friendlyByteBuf) {
        return new ItemResourceStorageMenu(
                menuType,
                id,
                inventory,
                new ItemStackHandler(27),
                UUID.randomUUID(),
                ContainerLevelAccess.NULL,
                player -> {

                },
                friendlyByteBuf != null ? friendlyByteBuf.readList(listBuf -> new Tab<>(
                        listBuf.readItem(),
                        listBuf.readList(subList -> subList.readWithCodec(Codecs.COMPONENT)),
                        listBuf.readEnum(ResourceScreenType.class)
                )) : Collections.emptyList()
        );
    }
}
