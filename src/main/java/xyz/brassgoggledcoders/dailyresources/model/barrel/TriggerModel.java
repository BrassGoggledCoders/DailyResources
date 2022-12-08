package xyz.brassgoggledcoders.dailyresources.model.barrel;

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

public class TriggerModel implements IUnbakedGeometry<TriggerModel> {
    private final BlockModel blockModel;

    public TriggerModel(BlockModel blockModel) {
        this.blockModel = blockModel;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter,
                           ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        return new TriggerBakedModel(
                modelLocation,
                spriteGetter,
                blockModel.bake(
                        bakery,
                        blockModel,
                        spriteGetter,
                        modelTransform,
                        modelLocation,
                        true
                )
        );
    }

    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext context, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
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
