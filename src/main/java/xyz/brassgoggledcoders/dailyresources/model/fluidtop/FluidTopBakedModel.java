package xyz.brassgoggledcoders.dailyresources.model.fluidtop;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.dailyresources.blockentity.FluidResourceStorageBlockEntity;
import xyz.brassgoggledcoders.dailyresources.model.BakedQuadBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FluidTopBakedModel implements BakedModel {
    private final BakedModel model;
    private final Function<Material, TextureAtlasSprite> spriteGetter;
    private final FluidModelInfo[] fluidInfo;

    public FluidTopBakedModel(BakedModel model, Function<Material, TextureAtlasSprite> spriteGetter, FluidModelInfo[] fluidInfo) {
        this.model = model;
        this.spriteGetter = spriteGetter;
        this.fluidInfo = fluidInfo;
    }

    @Override
    @NotNull
    public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pSide, @NotNull RandomSource pRand) {
        return new ArrayList<>(model.getQuads(pState, pSide, pRand, ModelData.EMPTY, null));
    }

    @Override
    @NotNull
    public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pSide, @NotNull RandomSource pRand, @NotNull ModelData modelData, @Nullable RenderType renderType) {
        List<BakedQuad> bakedQuads = new ArrayList<>(model.getQuads(pState, pSide, pRand, ModelData.EMPTY, renderType));

        if (renderType == RenderType.cutout()) {
            FluidStack[] fluids = modelData.get(FluidResourceStorageBlockEntity.TANK_FLUIDS_PROPERTY);

            if (fluids != null) {
                for (int i = 0; i < fluidInfo.length; i++) {
                    FluidStack fluidStack = FluidStack.EMPTY;

                    if (i < fluids.length) {
                        fluidStack = fluids[i];
                    }

                    if (!fluidStack.isEmpty()) {
                        FluidType fluidType = fluidStack.getFluid()
                                .getFluidType();

                        IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidType);
                        ResourceLocation stillTexture = fluidTypeExtensions.getStillTexture(fluidStack);
                        int color = fluidTypeExtensions.getTintColor(fluidStack);

                        FluidModelInfo info = fluidInfo[i];
                        bakedQuads.add(createBakedQuad(
                                getVertices(info, fluidStack),
                                Direction.UP,
                                spriteGetter.apply(new Material(
                                        InventoryMenu.BLOCK_ATLAS,
                                        stillTexture
                                )),
                                info.getUVs(),
                                new float[]{
                                        (color >> 16 & 0xFF) / 255.0F,
                                        (color >> 8 & 0xFF) / 255.0F,
                                        (color & 0xFF) / 255.0F,
                                        ((color >> 24) & 0xFF) / 255F
                                },
                                false
                        ));
                    }
                }
            }
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
        if (level.getBlockEntity(pos) instanceof FluidResourceStorageBlockEntity resourceStorageBlockEntity) {
            modelData = modelData.derive()
                    .with(FluidResourceStorageBlockEntity.TANK_FLUIDS_PROPERTY, resourceStorageBlockEntity.getTankFluids())
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

    private Vec3[] getVertices(FluidModelInfo modelInfo, FluidStack fluidStack) {
        Vec3[] vertices = new Vec3[4];

        Vector3f from = modelInfo.from();
        Vector3f to = modelInfo.to();

        float percentFull = 1F;
        if (fluidStack.getAmount() < 100) {
            percentFull = fluidStack.getAmount() / 100F;
        }

        float difference = from.y() - to.y();
        float height = to.y() + (difference * percentFull);

        vertices[0] = new Vec3(from.x(), height, from.z()).scale(0.0625);
        vertices[1] = new Vec3(from.x(), height, to.z()).scale(0.0625);
        vertices[2] = new Vec3(to.x(), height, to.z()).scale(0.0625);
        vertices[3] = new Vec3(to.x(), height, from.z()).scale(0.0625);
        return vertices;
    }

    public static BakedQuad createBakedQuad(Vec3[] vertices, Direction facing, TextureAtlasSprite sprite, double[] uvs, float[] colour, boolean invert) {
        BakedQuadBuilder builder = new BakedQuadBuilder();
        Vec3i normalInt = facing.getNormal();
        Vec3 faceNormal = new Vec3(normalInt.getX(), normalInt.getY(), normalInt.getZ());
        int vId = invert ? 3 : 0;
        int u = vId > 1 ? 2 : 0;
        builder.putVertexData(vertices[vId], faceNormal, uvs[u], uvs[1], sprite, colour, 1);
        vId = invert ? 2 : 1;
        builder.putVertexData(vertices[vId], faceNormal, uvs[u], uvs[3], sprite, colour, 1);
        vId = invert ? 1 : 2;
        u = vId > 1 ? 2 : 0;
        builder.putVertexData(vertices[vId], faceNormal, uvs[u], uvs[3], sprite, colour, 1);
        vId = invert ? 0 : 3;
        builder.putVertexData(vertices[vId], faceNormal, uvs[u], uvs[1], sprite, colour, 1);
        return builder.bake(-1, facing, sprite, true);
    }
}
