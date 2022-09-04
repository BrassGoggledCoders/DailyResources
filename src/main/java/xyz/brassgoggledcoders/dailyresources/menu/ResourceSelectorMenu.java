package xyz.brassgoggledcoders.dailyresources.menu;

import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.dailyresources.codec.Codecs;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesBlocks;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesResources;
import xyz.brassgoggledcoders.dailyresources.resource.Choice;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceGroup;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceType;
import xyz.brassgoggledcoders.dailyresources.screen.ResourceScreenType;
import xyz.brassgoggledcoders.dailyresources.screen.Tab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ResourceSelectorMenu<T> extends AbstractContainerMenu {
    private final DataSlot selectedGroupIndex = DataSlot.standalone();
    private final ContainerData selectedChoiceIndexes;

    private final List<Pair<UUID, ResourceGroup>> groupsToChoose;
    private final List<List<Choice<T>>> choices;

    private final ContainerLevelAccess levelAccess;
    private final Consumer<Player> closeHandler;
    private final Function4<UUID, ResourceGroup, Choice<T>, UUID, Boolean> onConfirmed;
    private final List<Tab<ResourceScreenType>> tabs;

    public ResourceSelectorMenu(MenuType<?> menuType, int menuId, Inventory inventory, ContainerLevelAccess levelAccess,
                                Consumer<Player> closeHandler, Function4<UUID, ResourceGroup, Choice<T>, UUID, Boolean> onConfirmed,
                                List<Pair<UUID, ResourceGroup>> groupsToChoose, ResourceType<T> resourceType,
                                List<Tab<ResourceScreenType>> tabs) {
        super(menuType, menuId);

        this.tabs = tabs;
        this.levelAccess = levelAccess;
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

        this.selectedChoiceIndexes = new BasicContainerData(this.groupsToChoose.size(), -1);

        this.addDataSlot(this.selectedGroupIndex);
        this.addDataSlots(this.selectedChoiceIndexes);
    }

    public List<Choice<T>> getChoices() {
        if (this.choices.isEmpty() || this.choices.size() < this.selectedGroupIndex.get()) {
            return Collections.emptyList();
        } else {
            return this.choices.get(this.selectedGroupIndex.get());
        }
    }

    public List<Choice<T>> getChoices(int groupIndex) {
        if (this.choices.isEmpty() || this.choices.size() < groupIndex) {
            return Collections.emptyList();
        } else {
            return this.choices.get(groupIndex);
        }
    }

    public int getNumChoices() {
        return this.getChoices().size();
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        return stillValid(levelAccess, pPlayer, DailyResourcesBlocks.BARREL.get());
    }

    @Override
    public boolean clickMenuButton(@NotNull Player pPlayer, int pId) {
        if (pId == this.getTabId()) {
            this.levelAccess.execute((level, blockPos) -> DailyResourcesBlocks.STORAGE_BLOCK_ENTITY.get(level, blockPos)
                    .ifPresent(storage -> storage.openMenu(pPlayer, ResourceScreenType.ITEM_STORAGE))
            );
            return true;
        } else if (pId >= this.getNumChoices() && pId < this.getNumChoices() + this.groupsToChoose.size()) {
            int groupIndex = pId - this.getNumChoices();
            if (this.hasValidGroupIndex(groupIndex)) {
                this.selectedGroupIndex.set(groupIndex);
                return true;
            } else {
                return false;
            }
        } else if (this.hasValidChoiceIndex(this.selectedGroupIndex.get(), pId)) {
            if (this.getSelectedChoiceIndex() == pId) {
                this.selectedChoiceIndexes.set(this.selectedGroupIndex.get(), -1);
            } else {
                this.selectedChoiceIndexes.set(this.selectedGroupIndex.get(), pId);
            }
            return true;
        }

        return false;
    }

    private boolean hasValidGroupIndex(int index) {
        return index >= 0 && index < this.groupsToChoose.size();
    }

    private boolean hasValidChoiceIndex(int groupIndex) {
        if (this.isValidGroupIndex(groupIndex)) {
            int index = this.selectedChoiceIndexes.get(groupIndex);
            return index >= 0 && index < this.getChoices().size();
        } else {
            return false;
        }
    }

    private boolean hasValidChoiceIndex(int groupIndex, int index) {
        if (this.isValidGroupIndex(groupIndex)) {
            return index >= 0 && index < this.getChoices().size();
        } else {
            return false;
        }
    }

    public int getSelectedChoiceIndex() {
        if (this.hasValidChoiceIndex(this.selectedGroupIndex.get())) {
            return this.selectedChoiceIndexes.get(this.selectedGroupIndex.get());
        } else {
            return -1;
        }
    }

    private boolean isValidGroupIndex(int index) {
        return index >= 0 && index < this.groupsToChoose.size();
    }

    @Override
    public void removed(@NotNull Player pPlayer) {
        super.removed(pPlayer);
        this.closeHandler.accept(pPlayer);
        for (int i = 0; i < this.getResourceGroups().size(); i++) {
            if (this.selectedChoiceIndexes.get(i) >= 0) {
                Pair<UUID, ResourceGroup> resourceGroupPair = this.getResourceGroups().get(i);
                Choice<T> choice = this.getChoice(i);
                if (choice != null) {
                    this.onConfirmed.apply(
                            resourceGroupPair.getFirst(),
                            resourceGroupPair.getSecond(),
                            choice,
                            pPlayer.getUUID()
                    );
                }
            }
        }
    }

    @Nullable
    public Choice<T> getChoice(int groupIndex) {
        if (this.hasValidChoiceIndex(groupIndex)) {
            return this.getChoices(groupIndex)
                    .get(this.selectedChoiceIndexes.get(groupIndex));
        } else {
            return null;
        }
    }

    public List<Pair<UUID, ResourceGroup>> getResourceGroups() {
        return this.groupsToChoose;
    }

    public int getSelectedGroupIndex() {
        return this.selectedGroupIndex.get();
    }

    public List<Tab<ResourceScreenType>> getTabs() {
        return this.tabs;
    }

    public int getTabId() {
        return this.getNumChoices() + this.getResourceGroups().size();
    }

    @NotNull
    public static <T> ResourceSelectorMenu<T> create(MenuType<ResourceSelectorMenu<T>> menuType, int id, Inventory inventory,
                                                     @Nullable FriendlyByteBuf friendlyByteBuf, ResourceType<T> resourceType) {
        return new ResourceSelectorMenu<>(
                menuType,
                id,
                inventory,
                ContainerLevelAccess.NULL,
                player -> {

                },
                (resourceGroup, resource, object, owner) -> false,
                friendlyByteBuf != null ? friendlyByteBuf.readList(listBuf -> Pair.of(
                        listBuf.readUUID(),
                        listBuf.readWithCodec(ResourceGroup.CODEC.get())
                )) : Collections.emptyList(),
                resourceType,
                friendlyByteBuf != null ? friendlyByteBuf.readList(listBuf -> new Tab<>(
                        listBuf.readItem(),
                        listBuf.readList(subList -> subList.readWithCodec(Codecs.COMPONENT)),
                        listBuf.readEnum(ResourceScreenType.class)
                )) : Collections.emptyList()
        );
    }

    @NotNull
    public static ResourceSelectorMenu<ItemStack> createItem(MenuType<ResourceSelectorMenu<ItemStack>> menuType, int id,
                                                             Inventory inventory, @Nullable FriendlyByteBuf friendlyByteBuf) {
        return create(menuType, id, inventory, friendlyByteBuf, DailyResourcesResources.ITEMSTACK.get());
    }
}