package xyz.brassgoggledcoders.dailyresources.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ResourceStorageMenu extends AbstractContainerMenu {
    private final UUID uniqueId;
    private final Predicate<Player> stillValid;
    private final Consumer<Player> closeHandler;

    private final IItemHandler itemHandler;

    public ResourceStorageMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory inventory, IItemHandler storageInventory, UUID uniqueId,
                               Predicate<Player> stillValid, Consumer<Player> closeHandler) {
        super(pMenuType, pContainerId);
        this.uniqueId = uniqueId;
        this.itemHandler = storageInventory;
        this.stillValid = stillValid;
        this.closeHandler = closeHandler;
        int containerRows = storageInventory.getSlots() / 9;
        int i = (containerRows - 4) * 18;

        for (int j = 0; j < containerRows; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new SlotItemHandler(storageInventory, k + j * 9, 8 + k * 18, 18 + j * 18));
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
        return this.stillValid.test(pPlayer);
    }

    @Override
    public void removed(@NotNull Player pPlayer) {
        super.removed(pPlayer);
        this.closeHandler.accept(pPlayer);
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    @NotNull
    public static ResourceStorageMenu create(MenuType<ResourceStorageMenu> menuType, int id, Inventory inventory) {
        return new ResourceStorageMenu(
                menuType,
                id,
                inventory,
                new ItemStackHandler(27),
                UUID.randomUUID(),
                player -> true,
                player -> {

                }
        );
    }
}
