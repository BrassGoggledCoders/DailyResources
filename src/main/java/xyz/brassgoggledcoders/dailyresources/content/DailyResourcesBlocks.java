package xyz.brassgoggledcoders.dailyresources.content;

import com.mojang.math.Vector3f;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import xyz.brassgoggledcoders.dailyresources.DailyResources;
import xyz.brassgoggledcoders.dailyresources.block.ResourceBarrelBlock;
import xyz.brassgoggledcoders.dailyresources.block.ResourceTankBlock;
import xyz.brassgoggledcoders.dailyresources.blockentity.FluidResourceStorageBlockEntity;
import xyz.brassgoggledcoders.dailyresources.blockentity.ItemResourceStorageBlockEntity;
import xyz.brassgoggledcoders.dailyresources.model.barrel.TriggerCustomModelBuilder;
import xyz.brassgoggledcoders.dailyresources.model.fluidtop.FluidModelInfo;
import xyz.brassgoggledcoders.dailyresources.model.fluidtop.FluidTopCustomModelBuilder;

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
            .loot(DailyResourcesBlocks::dropSelfWithInfo)
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
            .loot(DailyResourcesBlocks::dropSelfWithInfo)
            .addLayer(() -> RenderType::cutout)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .blockstate((context, provider) -> provider.horizontalBlock(
                    context.get(),
                    provider.models()
                            .withExistingParent(context.getName(), provider.mcLoc("block/block"))
                            .customLoader(TriggerCustomModelBuilder::new)
                            .withModel(provider.models()
                                    .nested()
                                    .customLoader(FluidTopCustomModelBuilder::new)
                                    .withModel(provider.models()
                                            .nested()
                                            .parent(provider.models().getExistingFile(provider.modLoc("block/tank")))
                                            .texture("tank_bottom", provider.modLoc("block/tank_bottom"))
                                            .texture("tank_side", provider.modLoc("block/tank_side"))
                                            .texture("tank_top", provider.modLoc("block/tank_top"))
                                    )
                                    .withFluidInfo(new FluidModelInfo(
                                            new Vector3f(2, 14, 2),
                                            new Vector3f(7, 2.01F, 7)
                                    ))
                                    .withFluidInfo(new FluidModelInfo(
                                            new Vector3f(9, 14, 2),
                                            new Vector3f(14, 2.01F, 7)
                                    ))
                                    .withFluidInfo(new FluidModelInfo(
                                            new Vector3f(9, 14, 9),
                                            new Vector3f(14, 2.01F, 14)
                                    ))
                                    .withFluidInfo(new FluidModelInfo(
                                            new Vector3f(2, 14, 9),
                                            new Vector3f(7, 2.01F, 14)
                                    ))
                                    .end()
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

    private static <T extends Block> void dropSelfWithInfo(RegistrateBlockLootTables lootTables, T block) {
        lootTables.add(block, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .name("drop")
                        .add(LootItem.lootTableItem(block)
                                .apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                        .copy("UniqueId", "BlockEntityTag.UniqueId")
                                        .copy("ResourceGroups", "BlockEntityTag.ResourceGroups")
                                )
                        )
                )
        );
    }
}
