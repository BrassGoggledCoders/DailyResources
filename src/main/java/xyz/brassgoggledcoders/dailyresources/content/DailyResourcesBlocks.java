package xyz.brassgoggledcoders.dailyresources.content;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.client.model.generators.ModelFile;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.block.ResourceBarrelBlock;
import xyz.brassgoggledcoders.dailyresources.block.ResourceTankBlock;
import xyz.brassgoggledcoders.dailyresources.blockentity.FluidResourceStorageBlockEntity;
import xyz.brassgoggledcoders.dailyresources.blockentity.ItemResourceStorageBlockEntity;
import xyz.brassgoggledcoders.dailyresources.model.barrel.TriggerCustomModelBuilder;

public class DailyResourcesBlocks {

    public static final BlockEntry<ResourceBarrelBlock> BARREL = DailyResources.getRegistrate()
            .object("barrel")
            .block(ResourceBarrelBlock::new)
            .initialProperties(Material.WOOD)
            .properties(properties -> properties.strength(2.5F)
                    .sound(SoundType.WOOD)
            )
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .addLayer(() -> RenderType::cutout)
            .blockstate((context, provider) -> {
                ModelFile openBarrel = provider.models().cubeBottomTop(
                        "block/barrel_open",
                        provider.mcLoc("block/barrel_side"),
                        provider.mcLoc("block/barrel_bottom"),
                        provider.mcLoc("block/barrel_top_open")
                );
                ModelFile closedBarrel = provider.models()
                        .withExistingParent("block/" + context.getName(), provider.mcLoc("block/block"))
                        .customLoader(TriggerCustomModelBuilder::new)
                        .withModel(provider.models().nested()
                                .parent(provider.models()
                                        .getExistingFile(provider.mcLoc("block/cube_bottom_top"))
                                )
                                .texture("side", provider.mcLoc("block/barrel_side"))
                                .texture("bottom", provider.mcLoc("block/barrel_bottom"))
                                .texture("top", provider.mcLoc("block/barrel_top"))
                        )
                        .end();
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

    public static final BlockEntry<ResourceTankBlock> TANK = DailyResources.getRegistrate()
            .object("iron_tank")
            .block(ResourceTankBlock::new)
            .initialProperties(Material.METAL)
            .properties(properties -> properties.strength(5.5F)
                    .sound(SoundType.METAL)
            )
            .addLayer(() -> RenderType::cutout)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .blockstate((context, provider) -> provider.horizontalBlock(
                    context.get(),
                    provider.models()
                            .withExistingParent(context.getName(), provider.mcLoc("block/block"))
                            .customLoader(TriggerCustomModelBuilder::new)
                            .withModel(provider.models()
                                    .nested()
                                    .parent(provider.models().getExistingFile(provider.modLoc("block/tank")))
                                    .texture("tank_bottom", provider.modLoc("block/tank_bottom"))
                                    .texture("tank_side", provider.modLoc("block/tank_side"))
                                    .texture("tank_top", provider.modLoc("block/tank_top"))
                            )
                            .end()

            ))
            .item()
            .build()
            .register();

    public static final BlockEntityEntry<FluidResourceStorageBlockEntity> FLUID_STORAGE_BLOCK_ENTITY = DailyResources.getRegistrate()
            .object("fluid_storage")
            .blockEntity(FluidResourceStorageBlockEntity::new)
            .register();

    public static void setup() {

    }
}
