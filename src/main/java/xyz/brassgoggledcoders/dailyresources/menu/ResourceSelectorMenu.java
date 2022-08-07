package xyz.brassgoggledcoders.dailyresources.menu;

import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.dailyresources.resource.Resource;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ResourceSelectorMenu extends AbstractContainerMenu {
    private final DataSlot selectedItemStackIndex = DataSlot.standalone();

    private final List<Pair<Resource, ItemStack>> itemStacks;

    private final Predicate<Player> stillValid;
    private final Consumer<Player> closeHandler;
    private final Function3<Resource, ItemStack, UUID, Void> onConfirmed;

    public ResourceSelectorMenu(MenuType<?> menuType, int menuId, Inventory inventory, Predicate<Player> stillValid,
                                Consumer<Player> closeHandler, Function3<Resource, ItemStack, UUID, Void> onConfirmed,
                                List<Pair<Resource, ItemStack>> choices) {
        super(menuType, menuId);

        this.stillValid = stillValid;
        this.closeHandler = closeHandler;
        this.onConfirmed = onConfirmed;
        this.itemStacks = choices;

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
        }

        this.addDataSlot(this.selectedItemStackIndex);
    }

    /**
     * Returns the index of the selected recipe.
     */
    public int getSelectedItemStackIndex() {
        return this.selectedItemStackIndex.get();
    }

    public List<Pair<Resource, ItemStack>> getItemStacks() {
        return this.itemStacks;
    }

    public int getNumItemStacks() {
        return this.itemStacks.size();
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return this.stillValid.test(pPlayer);
    }

    @Override
    public boolean clickMenuButton(@NotNull Player pPlayer, int pId) {
        if (pId == -1) {
            if (this.isValidItemStackIndex(this.selectedItemStackIndex.get())) {
                Pair<Resource, ItemStack> selected = this.getItemStacks().get(this.selectedItemStackIndex.get());
                this.onConfirmed.apply(selected.getFirst(), selected.getSecond(), pPlayer.getUUID());
                if (pPlayer instanceof ServerPlayer serverPlayer) {
                    serverPlayer.closeContainer();
                }
            } else {
                return false;
            }
        } else if (this.isValidItemStackIndex(pId)) {
            this.selectedItemStackIndex.set(pId);
        }

        return true;
    }

    private boolean isValidItemStackIndex(int index) {
        return index >= 0 && index < this.itemStacks.size();
    }

    @Override
    public void removed(@NotNull Player pPlayer) {
        super.removed(pPlayer);
        this.closeHandler.accept(pPlayer);
    }

    @NotNull
    public static ResourceSelectorMenu create(MenuType<ResourceSelectorMenu> menuType, int id, Inventory inventory,
                                              @Nullable FriendlyByteBuf friendlyByteBuf) {
        return new ResourceSelectorMenu(
                menuType,
                id,
                inventory,
                player -> true,
                player -> {

                },
                (resource, itemStack, owner) -> null,
                friendlyByteBuf != null ? friendlyByteBuf.readList(listBuf -> Pair.of(
                        listBuf.readWithCodec(Resource.CODEC.get()),
                        listBuf.readItem()
                )) : Collections.emptyList()
        );
    }
}
