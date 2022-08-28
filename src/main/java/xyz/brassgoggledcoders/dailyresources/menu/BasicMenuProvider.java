package xyz.brassgoggledcoders.dailyresources.menu;

import com.mojang.datafixers.util.Function3;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

public class BasicMenuProvider<T extends AbstractContainerMenu> implements MenuProvider {
    private final Component displayName;
    private final Function3<Integer, Inventory, Player, T> createMenu;

    public BasicMenuProvider(Component displayName, Function3<Integer, Inventory, Player, T> createMenu) {
        this.displayName = displayName;
        this.createMenu = createMenu;
    }

    @Override
    @NotNull
    public Component getDisplayName() {
        return this.displayName;
    }

    @Nullable
    @Override
    @ParametersAreNonnullByDefault
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        return createMenu.apply(pContainerId, pInventory, pPlayer);
    }
}
