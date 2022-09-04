package xyz.brassgoggledcoders.dailyresources.content;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.MenuEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.client.model.generators.ModelFile;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.block.ResourceBarrelBlock;
import xyz.brassgoggledcoders.dailyresources.block.ResourceTankBlock;
import xyz.brassgoggledcoders.dailyresources.blockentity.ItemResourceStorageBlockEntity;
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
            .addLayer(() -> RenderType::cutout)
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

    public static final BlockEntityEntry<ItemResourceStorageBlockEntity> STORAGE_BLOCK_ENTITY =
            DailyResources.getRegistrate()
                    .object("storage")
                    .blockEntity(ItemResourceStorageBlockEntity::new)
                    .validBlock(BARREL)
                    .register();

    public static final MenuEntry<ResourceStorageMenu> STORAGE_MENU = DailyResources.getRegistrate()
            .object("storage")
            .menu(ResourceStorageMenu::create, () -> ResourceStorageScreen::new)
            .register();

    public static final MenuEntry<ResourceSelectorMenu<ItemStack>> ITEM_SELECTOR_MENU = DailyResources.getRegistrate()
            .object("selector")
            .<ResourceSelectorMenu<ItemStack>, ResourceSelectorScreen<ItemStack>>menu(
                    ResourceSelectorMenu::createItem,
                    () -> ResourceSelectorScreen::new
            )
            .register();

    public static final BlockEntry<ResourceTankBlock> TANK = DailyResources.getRegistrate()
            .object("iron_tank")
            .block(ResourceTankBlock::new)
            .initialProperties(Material.METAL)
            .properties(properties -> properties.strength(5.5F)
                    .sound(SoundType.METAL)
            )
            .blockstate((context, provider) -> provider.simpleBlock(
                    context.get(),
                    provider.models()
                            .withExistingParent(context.getName(), provider.modLoc("block/tank"))
                            .texture("barrel_bottom", provider.modLoc("block/barrel_bottom"))
                            .texture("barrel_side", provider.modLoc("block/barrel_side"))
                            .texture("barrel_top", provider.modLoc("block/barrel_top"))
            ))
            .item()
            .build()
            .register();

    public static void setup() {

    }
}
