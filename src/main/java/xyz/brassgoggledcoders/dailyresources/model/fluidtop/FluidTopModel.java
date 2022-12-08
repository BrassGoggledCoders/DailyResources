package xyz.brassgoggledcoders.dailyresources.model.fluidtop;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesTriggers;
import xyz.brassgoggledcoders.dailyresources.trigger.Trigger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class FluidTopModel implements IUnbakedGeometry<FluidTopModel> {
    private final BlockModel blockModel;
    private final FluidModelInfo[] fluidInfo;

    public FluidTopModel(BlockModel blockModel, FluidModelInfo[] fluidInfo) {
        this.blockModel = blockModel;
        this.fluidInfo = fluidInfo;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter,
                           ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        return new FluidTopBakedModel(
                blockModel.bake(
                        bakery,
                        blockModel,
                        spriteGetter,
                        modelTransform,
                        modelLocation,
                        true
                ),
                spriteGetter,
                fluidInfo
        );
    }

    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        Set<Material> materials = new HashSet<>(blockModel.getMaterials(modelGetter, missingTextureErrors));
        DailyResourcesTriggers.getRegistry()
                .getValues()
                .stream()
                .map(Trigger::texture)
                .map(texture -> new Material(InventoryMenu.BLOCK_ATLAS, texture))
                .forEach(materials::add);
        return materials;
    }
}
