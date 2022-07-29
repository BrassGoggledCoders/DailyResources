package xyz.brassgoggledcoders.dailyresources.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ResourceStorageMenu extends AbstractContainerMenu {
    private final UUID uniqueId;
    private final Predicate<Player> stillValid;
    private final Consumer<Player> closeHandler;

    public ResourceStorageMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory inventory, UUID uniqueId,
                               Predicate<Player> stillValid, Consumer<Player> closeHandler) {
        super(pMenuType, pContainerId);
        this.uniqueId = uniqueId;
        this.stillValid = stillValid;
        this.closeHandler = closeHandler;
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
                UUID.randomUUID(),
                player -> true,
                player -> {

                }
        );
    }
}
