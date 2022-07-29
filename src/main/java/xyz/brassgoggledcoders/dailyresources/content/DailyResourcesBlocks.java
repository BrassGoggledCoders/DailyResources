package xyz.brassgoggledcoders.dailyresources.content;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.MenuEntry;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.client.model.generators.ModelFile;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.block.ResourceBarrelBlock;
import xyz.brassgoggledcoders.dailyresources.blockentity.ResourceStorageBlockEntity;
import xyz.brassgoggledcoders.dailyresources.menu.ResourceSelectorMenu;
import xyz.brassgoggledcoders.dailyresources.menu.ResourceStorageMenu;
import xyz.brassgoggledcoders.dailyresources.screen.ResourceSelectorScreen;
import xyz.brassgoggledcoders.dailyresources.screen.ResourceStorageScreen;

public class DailyResourcesBlocks {

    public static final BlockEntry<ResourceBarrelBlock> BARREL = DailyResources.getRegistrate()
            .object("barrel")
            .block(ResourceBarrelBlock::new)
            .initialProperties(Material.WOOD)
            .properties(properties -> properties.strength(2.5F)
                    .sound(SoundType.WOOD)
            )
            .blockstate((context, provider) -> {
                ModelFile openBarrel = provider.models().cubeBottomTop(
                        "block/barrel_open",
                        provider.mcLoc("block/barrel_side"),
                        provider.mcLoc("block/barrel_bottom"),
                        provider.mcLoc("block/barrel_top_open")
                );
                ModelFile closedBarrel = provider.models().cubeBottomTop(
                        "block/barrel",
                        provider.mcLoc("block/barrel_side"),
                        provider.mcLoc("block/barrel_bottom"),
                        provider.mcLoc("block/barrel_top")
                );
                provider.directionalBlock(context.get(), blockState -> {
                    if (blockState.getValue(ResourceBarrelBlock.OPEN)) {
                        return openBarrel;
                    } else {
                        return closedBarrel;
                    }
                });
            })
            .item()
            .build()
            .register();

    public static final BlockEntityEntry<ResourceStorageBlockEntity> STORAGE_BLOCK_ENTITY =
            DailyResources.getRegistrate()
                    .object("storage")
                    .<ResourceStorageBlockEntity>blockEntity(ResourceStorageBlockEntity::new)
                    .validBlock(BARREL)
                    .register();

    public static final MenuEntry<ResourceStorageMenu> STORAGE_MENU = DailyResources.getRegistrate()
            .object("storage")
            .menu(ResourceStorageMenu::create, () -> ResourceStorageScreen::new)
            .register();

    public static final MenuEntry<ResourceSelectorMenu> SELECTOR_MENU = DailyResources.getRegistrate()
            .object("selector")
            .menu(ResourceSelectorMenu::create, () -> ResourceSelectorScreen::new)
            .register();

    public static void setup() {

    }
}
