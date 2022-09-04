package xyz.brassgoggledcoders.dailyresources.screen;

import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import xyz.brassgoggledcoders.dailyresources.blockentity.ItemResourceStorageBlockEntity;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesBlocks;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesContainers;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesResources;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesText;
import xyz.brassgoggledcoders.dailyresources.menu.ResourceSelectorMenu;
import xyz.brassgoggledcoders.dailyresources.menu.ResourceStorageMenu;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceGroup;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public enum ResourceScreenType {
    ITEM_SELECTOR(DailyResourcesText.SELECTION) {
        @Override
        public Function3<Integer, Inventory, Player, AbstractContainerMenu> getMenuFunction(
                BlockEntity blockEntity
        ) {
            if (blockEntity instanceof ItemResourceStorageBlockEntity resourceStorageBlockEntity) {
                return (containerId, inventory, player) -> new ResourceSelectorMenu<>(
                        DailyResourcesContainers.ITEM_SELECTOR_MENU.get(),
                        containerId,
                        inventory,
                        resourceStorageBlockEntity.createLevelAccess(),
                        resourceStorageBlockEntity::stopOpen,
                        resourceStorageBlockEntity::onConfirmed,
                        resourceStorageBlockEntity.getCachedGroupsForChoices(),
                        DailyResourcesResources.ITEMSTACK.get(),
                        this.getTabs(
                                blockEntity.getBlockState().getBlock(),
                                resourceStorageBlockEntity.getCachedGroupsForChoices()
                        )
                );
            } else {
                return null;
            }
        }

        @Override
        public Tab<ResourceScreenType> getTab(Block block, List<Pair<UUID, ResourceGroup>> choices) {
            List<Component> components = choices.stream()
                    .map(pair -> new TextComponent(" * ")
                            .append(pair.getSecond().name())
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
                    )
                    .collect(Collectors.toList());
            components.add(0, this.getDisplayName());

            return new Tab<>(
                    new ItemStack(block),
                    components,
                    this
            );
        }

        @Override
        public List<Tab<ResourceScreenType>> getTabs(Block block, List<Pair<UUID, ResourceGroup>> groups) {
            return Arrays.stream(ResourceScreenType.values())
                    .map(type -> type.getTab(block, groups))
                    .collect(Collectors.toList());
        }
    },
    ITEM_STORAGE(DailyResourcesText.STORAGE) {
        @Override
        public Function3<Integer, Inventory, Player, AbstractContainerMenu> getMenuFunction(
                BlockEntity blockEntity
        ) {
            if (blockEntity instanceof ItemResourceStorageBlockEntity resourceStorageBlockEntity) {
                return (containerId, inventory, player) -> new ResourceStorageMenu(
                        DailyResourcesContainers.STORAGE_MENU.get(),
                        containerId,
                        inventory,
                        resourceStorageBlockEntity.getHandler(),
                        resourceStorageBlockEntity.getUniqueId(),
                        resourceStorageBlockEntity.createLevelAccess(),
                        resourceStorageBlockEntity::stopOpen,
                        this.getTabs(
                                resourceStorageBlockEntity.getBlockState().getBlock(),
                                resourceStorageBlockEntity.getCachedGroupsForChoices()
                        )
                );
            } else {
                return null;
            }
        }

        @Override
        public Tab<ResourceScreenType> getTab(Block block, List<Pair<UUID, ResourceGroup>> choices) {
            return new Tab<>(
                    new ItemStack(block),
                    Collections.singletonList(this.getDisplayName()),
                    this
            );
        }

        @Override
        public List<Tab<ResourceScreenType>> getTabs(Block block, List<Pair<UUID, ResourceGroup>> choices) {
            if (choices.isEmpty()) {
                return Collections.emptyList();
            } else {
                return ResourceScreenType.ITEM_SELECTOR.getTabs(block, choices);
            }
        }
    };

    private final Component displayName;

    ResourceScreenType(Component displayName) {
        this.displayName = displayName;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public abstract Function3<Integer, Inventory, Player, AbstractContainerMenu> getMenuFunction(BlockEntity blockEntity);

    public abstract Tab<ResourceScreenType> getTab(Block block, List<Pair<UUID, ResourceGroup>> choices);

    public abstract List<Tab<ResourceScreenType>> getTabs(Block block, List<Pair<UUID, ResourceGroup>> choices);
}
