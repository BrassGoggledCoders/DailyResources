package xyz.brassgoggledcoders.dailyresources.model.barrel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.dailyresources.blockentity.ResourceStorageBlockEntity;
import xyz.brassgoggledcoders.dailyresources.trigger.Trigger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class TriggerBakedModel implements BakedModel {
    private final ResourceLocation modelLocation;
    private final BakedModel model;
    private final Function<Material, TextureAtlasSprite> spriteGetter;

    public TriggerBakedModel(ResourceLocation modelLocation, Function<Material, TextureAtlasSprite> spriteGetter, BakedModel model) {
        this.modelLocation = modelLocation;
        this.model = model;
        this.spriteGetter = spriteGetter;
    }

    @Override
    @NotNull
    public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pSide, @NotNull Random pRand) {
        return new ArrayList<>(model.getQuads(pState, pSide, pRand, EmptyModelData.INSTANCE));
    }

    @Override
    @NotNull
    public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pSide, @NotNull Random pRand, @NotNull IModelData modelData) {
        List<BakedQuad> bakedQuads = new ArrayList<>(model.getQuads(pState, pSide, pRand, EmptyModelData.INSTANCE));
        Trigger trigger = modelData.getData(ResourceStorageBlockEntity.TRIGGER_PROPERTY);
        if (trigger != null) {
            Direction facing;
            if (pState == null) {
                facing = Direction.NORTH;
            } else if (pState.hasProperty(BlockStateProperties.FACING)) {
                facing = pState.getValue(BlockStateProperties.FACING);
            } else if (pState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                facing = pState.getValue(BlockStateProperties.HORIZONTAL_FACING);
            } else {
                facing = Direction.NORTH;
            }
            bakedQuads.add(BlockModel.makeBakedQuad(
                    new BlockElement(
                            new Vector3f(0, 0, 0),
                            new Vector3f(16, 16, 16),
                            new HashMap<>(),
                            null,
                            true
                    ),
                    new BlockElementFace(
                            facing.getOpposite(),
                            1,
                            trigger.getTexture().toString(),
                            new BlockFaceUV(
                                    new float[]{
                                            0, 0, 16, 16
                                    },
                                    0
                            )
                    ),
                    spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, trigger.getTexture())),
                    facing,
                    SimpleModelState.IDENTITY,
                    modelLocation
            ));
        }
        return bakedQuads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return model.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return model.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return model.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    @NotNull
    @SuppressWarnings("deprecation")
    public TextureAtlasSprite getParticleIcon() {
        return model.getParticleIcon();
    }

    @Override
    @NotNull
    public ItemOverrides getOverrides() {
        return model.getOverrides();
    }

    @NotNull
    @Override
    public IModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull IModelData modelData) {
        if (level.getBlockEntity(pos) instanceof ResourceStorageBlockEntity resourceStorageBlockEntity) {
            return new ModelDataMap.Builder()
                    .withInitial(ResourceStorageBlockEntity.TRIGGER_PROPERTY, resourceStorageBlockEntity.getTrigger())
                    .build();
        } else {
            return EmptyModelData.INSTANCE;
        }
    }

    @Override
    public BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack poseStack) {
        return model.handlePerspective(cameraTransformType, poseStack);
    }
}
