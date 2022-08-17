package xyz.brassgoggledcoders.dailyresources.menu;

import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesResources;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceGroup;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ResourceSelectorMenu<T> extends AbstractContainerMenu {
    private final DataSlot selectedGroupIndex = DataSlot.standalone();
    private final DataSlot selectedChoiceIndex = DataSlot.standalone();

    private final List<Pair<UUID, ResourceGroup>> groupsToChoose;
    private final List<List<Choice<T>>> choices;

    private final Predicate<Player> stillValid;
    private final Consumer<Player> closeHandler;
    private final Function4<UUID, ResourceGroup, Choice<T>, UUID, Boolean> onConfirmed;

    public ResourceSelectorMenu(MenuType<?> menuType, int menuId, Inventory inventory, Predicate<Player> stillValid,
                                Consumer<Player> closeHandler, Function4<UUID, ResourceGroup, Choice<T>, UUID, Boolean> onConfirmed,
                                List<Pair<UUID, ResourceGroup>> groupsToChoose, ResourceType<T> resourceType) {
        super(menuType, menuId);

        this.stillValid = stillValid;
        this.closeHandler = closeHandler;
        this.onConfirmed = onConfirmed;
        this.groupsToChoose = groupsToChoose;
        if (resourceType == null) {
            this.choices = new ArrayList<>();
        } else {
            this.choices = this.groupsToChoose.stream()
                    .map(Pair::getSecond)
                    .map(resourceGroup -> resourceGroup.getChoicesFor(resourceType))
                    .toList();
        }

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
        }

        this.addDataSlot(this.selectedChoiceIndex);
    }

    /**
     * Returns the index of the selected recipe.
     */
    public int getSelectedChoiceIndex() {
        return this.selectedChoiceIndex.get();
    }

    public List<Choice<T>> getChoices() {
        if (this.choices.isEmpty() || this.choices.size() < this.selectedGroupIndex.get()) {
            return Collections.emptyList();
        } else {
            return this.choices.get(this.selectedChoiceIndex.get());
        }
    }

    public int getNumItemStacks() {
        return this.choices.size();
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return this.stillValid.test(pPlayer);
    }

    @Override
    public boolean clickMenuButton(@NotNull Player pPlayer, int pId) {
        if (pId == -1) {
            if (this.isValidChoiceIndex(this.selectedChoiceIndex.get())) {
                Pair<UUID, ResourceGroup> selectedGroup = this.getSelectedGroup();
                Choice<T> selected = this.getSelectedChoice();
                if (selectedGroup != null && selected != null) {
                    return this.onConfirmed.apply(
                            selectedGroup.getFirst(),
                            selectedGroup.getSecond(),
                            selected,
                            pPlayer.getUUID()
                    );
                }
            } else {
                return false;
            }
        } else if (this.isValidChoiceIndex(pId)) {
            this.selectedChoiceIndex.set(pId);
        }

        return true;
    }

    private boolean isValidChoiceIndex(int index) {
        return index >= 0 && index < this.getChoices().size();
    }

    private Choice<T> getSelectedChoice() {
        if (this.isValidChoiceIndex(this.selectedChoiceIndex.get())) {
            return this.getChoices().get(this.selectedChoiceIndex.get());
        } else {
            return null;
        }
    }

    private boolean isValidGroupIndex(int index) {
        return index >= 0 && index < this.groupsToChoose.size();
    }

    private Pair<UUID, ResourceGroup> getSelectedGroup() {
        if (this.isValidGroupIndex(this.selectedGroupIndex.get())) {
            return this.groupsToChoose.get(this.selectedGroupIndex.get());
        } else {
            return null;
        }
    }

    @Override
    public void removed(@NotNull Player pPlayer) {
        super.removed(pPlayer);
        this.closeHandler.accept(pPlayer);
    }

    @NotNull
    public static <T> ResourceSelectorMenu<T> create(MenuType<ResourceSelectorMenu<T>> menuType, int id, Inventory inventory,
                                                     @Nullable FriendlyByteBuf friendlyByteBuf, ResourceType<T> resourceType) {
        return new ResourceSelectorMenu<>(
                menuType,
                id,
                inventory,
                player -> true,
                player -> {

                },
                (resourceGroup, resource, object, owner) -> false,
                friendlyByteBuf != null ? friendlyByteBuf.readList(listBuf -> Pair.of(
                        listBuf.readUUID(),
                        listBuf.readWithCodec(ResourceGroup.CODEC.get())
                )) : Collections.emptyList(),
                resourceType
        );
    }

    @NotNull
    public static ResourceSelectorMenu<ItemStack> createItem(MenuType<ResourceSelectorMenu<ItemStack>> menuType, int id,
                                                             Inventory inventory, @Nullable FriendlyByteBuf friendlyByteBuf) {
        return create(menuType, id, inventory, friendlyByteBuf, DailyResourcesResources.ITEMSTACK.get());
    }
}