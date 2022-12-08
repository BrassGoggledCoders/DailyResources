package xyz.brassgoggledcoders.dailyresources.screen;

import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.dailyresources.blockentity.FluidResourceStorageBlockEntity;
import xyz.brassgoggledcoders.dailyresources.blockentity.ItemResourceStorageBlockEntity;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesContainers;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesResources;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesText;
import xyz.brassgoggledcoders.dailyresources.menu.FluidResourceStorageMenu;
import xyz.brassgoggledcoders.dailyresources.menu.ItemResourceStorageMenu;
import xyz.brassgoggledcoders.dailyresources.menu.ResourceSelectorMenu;
import xyz.brassgoggledcoders.dailyresources.resource.ResourceGroup;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ResourceScreenType {
    ITEM_SELECTOR(DailyResourcesText.SELECTION, true) {
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
                    .map(pair -> Component.literal(" * ")
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
        public List<Tab<ResourceScreenType>> getTabs(Block block, @NotNull List<Pair<UUID, ResourceGroup>> groups) {
            return Stream.of(this, ITEM_STORAGE)
                    .map(type -> type.getTab(block, groups))
                    .collect(Collectors.toList());
        }
    },
    ITEM_STORAGE(DailyResourcesText.STORAGE, false) {
        @Override
        public Function3<Integer, Inventory, Player, AbstractContainerMenu> getMenuFunction(
                BlockEntity blockEntity
        ) {
            if (blockEntity instanceof ItemResourceStorageBlockEntity resourceStorageBlockEntity) {
                return (containerId, inventory, player) -> new ItemResourceStorageMenu(
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
        public List<Tab<ResourceScreenType>> getTabs(Block block, @NotNull List<Pair<UUID, ResourceGroup>> choices) {
            if (choices.isEmpty()) {
                return Collections.emptyList();
            } else {
                return ResourceScreenType.ITEM_SELECTOR.getTabs(block, choices);
            }
        }
    },
    FLUID_SELECTOR(DailyResourcesText.SELECTION, true) {
        @Override
        public Function3<Integer, Inventory, Player, AbstractContainerMenu> getMenuFunction(
                BlockEntity blockEntity
        ) {
            if (blockEntity instanceof FluidResourceStorageBlockEntity resourceStorageBlockEntity) {
                return (containerId, inventory, player) -> new ResourceSelectorMenu<>(
                        DailyResourcesContainers.FLUID_SELECTOR_MENU.get(),
                        containerId,
                        inventory,
                        resourceStorageBlockEntity.createLevelAccess(),
                        (value) -> {
                        },
                        resourceStorageBlockEntity::onConfirmed,
                        resourceStorageBlockEntity.getCachedGroupsForChoices(),
                        DailyResourcesResources.FLUIDSTACK.get(),
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
                    .map(pair -> Component.literal(" * ")
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
        public List<Tab<ResourceScreenType>> getTabs(Block block, @NotNull List<Pair<UUID, ResourceGroup>> groups) {
            return Stream.of(this, FLUID_STORAGE)
                    .map(type -> type.getTab(block, groups))
                    .collect(Collectors.toList());
        }
    },
    FLUID_STORAGE(DailyResourcesText.STORAGE, false) {
        @Override
        public Function3<Integer, Inventory, Player, AbstractContainerMenu> getMenuFunction(
                BlockEntity blockEntity
        ) {
            if (blockEntity instanceof FluidResourceStorageBlockEntity resourceStorageBlockEntity) {
                return (containerId, inventory, player) -> new FluidResourceStorageMenu(
                        DailyResourcesContainers.FLUID_STORAGE_MENU.get(),
                        containerId,
                        inventory,
                        resourceStorageBlockEntity.getHandler(),
                        resourceStorageBlockEntity.createLevelAccess(),
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
        public List<Tab<ResourceScreenType>> getTabs(Block block, @NotNull List<Pair<UUID, ResourceGroup>> choices) {
            if (choices.isEmpty()) {
                return Collections.emptyList();
            } else {
                return ResourceScreenType.FLUID_SELECTOR.getTabs(block, choices);
            }
        }
    };

    private final Component displayName;
    private final boolean selector;

    ResourceScreenType(Component displayName, boolean selector) {
        this.displayName = displayName;
        this.selector = selector;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public abstract Function3<Integer, Inventory, Player, AbstractContainerMenu> getMenuFunction(BlockEntity blockEntity);

    public abstract Tab<ResourceScreenType> getTab(Block block, List<Pair<UUID, ResourceGroup>> choices);

    public abstract List<Tab<ResourceScreenType>> getTabs(Block block, @NotNull List<Pair<UUID, ResourceGroup>> choices);

    public boolean isSelector() {
        return this.selector;
    }
}
