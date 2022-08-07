package xyz.brassgoggledcoders.dailyresources.model.barrel;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import xyz.brassgoggledcoders.dailyresources.content.DailyResourcesTriggers;
import xyz.brassgoggledcoders.dailyresources.trigger.Trigger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class ResourceBarrelModel implements IModelGeometry<ResourceBarrelModel> {
    private final BlockModel blockModel;

    public ResourceBarrelModel(BlockModel blockModel) {
        this.blockModel = blockModel;
    }

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter,
                           ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        return new ResourceBarrelBakedModel(
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
    public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        Set<Material> materials = new HashSet<>(blockModel.getMaterials(modelGetter, missingTextureErrors));
        DailyResourcesTriggers.REGISTRY.get()
                .getValues()
                .stream()
                .map(Trigger::getTexture)
                .map(texture -> new Material(InventoryMenu.BLOCK_ATLAS, texture))
                .forEach(materials::add);
        return materials;
    }
}
