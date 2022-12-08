package xyz.brassgoggledcoders.dailyresources.model.barrel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.dailyresources.blockentity.ResourceStorageBlockEntity;
import xyz.brassgoggledcoders.dailyresources.trigger.Trigger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pSide, @NotNull RandomSource pRand) {
        return new ArrayList<>(model.getQuads(pState, pSide, pRand, ModelData.EMPTY, RenderType.solid()));
    }

    @Override
    @NotNull
    public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pSide, @NotNull RandomSource pRand, @NotNull ModelData modelData, @Nullable RenderType renderType) {
        List<BakedQuad> bakedQuads = new ArrayList<>(model.getQuads(pState, pSide, pRand, modelData, renderType));
        Trigger trigger = modelData.get(ResourceStorageBlockEntity.TRIGGER_PROPERTY);
        if (trigger != null && renderType == RenderType.cutout()) {
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
            bakedQuads.add(BlockModel.bakeFace(
                    new BlockElement(
                            new Vector3f(-0.01F, -0.01F, -0.01F),
                            new Vector3f(16.01F, 16.01F, 16.01F),
                            new HashMap<>(),
                            null,
                            true
                    ),
                    new BlockElementFace(
                            facing.getOpposite(),
                            1,
                            trigger.texture().toString(),
                            new BlockFaceUV(
                                    new float[]{
                                            0, 0, 16, 16
                                    },
                                    0
                            )
                    ),
                    spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, trigger.texture())),
                    facing,
                    new SimpleModelState(Transformation.identity()),
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
    public ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
        modelData = model.getModelData(level, pos, state, modelData);
        if (level.getBlockEntity(pos) instanceof ResourceStorageBlockEntity<?> resourceStorageBlockEntity) {
            modelData = modelData.derive()
                    .with(ResourceStorageBlockEntity.TRIGGER_PROPERTY, resourceStorageBlockEntity.getTrigger())
                    .build();
        }
        return modelData;
    }

    @Override
    @NotNull
    @ParametersAreNonnullByDefault
    public BakedModel applyTransform(ItemTransforms.TransformType transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        this.model.applyTransform(transformType, poseStack, applyLeftHandTransform);
        return this;
    }

    @Override
    @NotNull
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        return ChunkRenderTypeSet.union(
                this.model.getRenderTypes(state, rand, data),
                ChunkRenderTypeSet.of(RenderType.cutout())
        );
    }

    @Override
    @NotNull
    public List<RenderType> getRenderTypes(@NotNull ItemStack itemStack, boolean fabulous) {
        List<RenderType> renderTypes = new ArrayList<>(this.model.getRenderTypes(itemStack, fabulous));
        renderTypes.add(RenderType.cutout());
        return renderTypes;
    }
}
