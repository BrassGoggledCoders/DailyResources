package xyz.brassgoggledcoders.dailyresources.model.fluidtop;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Vector3f;
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
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.brassgoggledcoders.dailyresources.blockentity.FluidResourceStorageBlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
    public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pSide, @NotNull Random pRand) {
        return new ArrayList<>(model.getQuads(pState, pSide, pRand, EmptyModelData.INSTANCE));
    }

    @Override
    @NotNull
    public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pSide, @NotNull Random pRand, @NotNull IModelData modelData) {
        List<BakedQuad> bakedQuads = new ArrayList<>(model.getQuads(pState, pSide, pRand, EmptyModelData.INSTANCE));

        FluidStack[] fluids = modelData.getData(FluidResourceStorageBlockEntity.TANK_FLUIDS_PROPERTY);

        if (fluids != null) {
            for (int i = 0; i < fluidInfo.length; i++) {
                FluidStack fluidStack = FluidStack.EMPTY;

                if (i < fluids.length) {
                    fluidStack = fluids[i];
                }

                if (!fluidStack.isEmpty()) {
                    FluidAttributes fluidAttributes = fluidStack.getFluid()
                            .getAttributes();

                    ResourceLocation stillTexture = fluidAttributes.getStillTexture(fluidStack);
                    int color = fluidAttributes.getColor(fluidStack);

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
        modelData = model.getModelData(level, pos, state, modelData);
        if (level.getBlockEntity(pos) instanceof FluidResourceStorageBlockEntity resourceStorageBlockEntity) {
            modelData.setData(FluidResourceStorageBlockEntity.TANK_FLUIDS_PROPERTY, resourceStorageBlockEntity.getTankFluids());
        }
        return modelData;
    }

    @Override
    public BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack poseStack) {
        return model.handlePerspective(cameraTransformType, poseStack);
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

    @SuppressWarnings("ReassignedVariable")
    public static BakedQuad createBakedQuad(Vec3[] vertices, Direction facing, TextureAtlasSprite sprite, double[] uvs, float[] colour, boolean invert) {
        BakedQuadBuilder builder = new BakedQuadBuilder(sprite);
        builder.setQuadOrientation(facing);
        builder.setApplyDiffuseLighting(true);
        Vec3i normalInt = facing.getNormal();
        Vec3 faceNormal = new Vec3(normalInt.getX(), normalInt.getY(), normalInt.getZ());
        int vId = invert ? 3 : 0;
        int u = vId > 1 ? 2 : 0;
        putVertexData(builder, vertices[vId], faceNormal, uvs[u], uvs[1], sprite, colour, 1);
        vId = invert ? 2 : 1;
        u = vId > 1 ? 2 : 0;
        putVertexData(builder, vertices[vId], faceNormal, uvs[u], uvs[3], sprite, colour, 1);
        vId = invert ? 1 : 2;
        u = vId > 1 ? 2 : 0;
        putVertexData(builder, vertices[vId], faceNormal, uvs[u], uvs[3], sprite, colour, 1);
        vId = invert ? 0 : 3;
        u = vId > 1 ? 2 : 0;
        putVertexData(builder, vertices[vId], faceNormal, uvs[u], uvs[1], sprite, colour, 1);
        return builder.build();
    }

    public static void putVertexData(BakedQuadBuilder builder, Vec3 pos, Vec3 faceNormal, double u, double v, TextureAtlasSprite sprite, float[] colour, float alpha) {
        VertexFormat format = DefaultVertexFormat.BLOCK;
        for (int e = 0; e < format.getElements().size(); e++)
            switch (format.getElements().get(e).getUsage()) {
                case POSITION:
                    builder.put(e, (float) pos.x, (float) pos.y, (float) pos.z);
                    break;
                case COLOR:
                    float d = 1;//LightUtil.diffuseLight(faceNormal.x, faceNormal.y, faceNormal.z);
                    builder.put(e, d * colour[0], d * colour[1], d * colour[2], 1 * colour[3] * alpha);
                    break;
                case UV:
                    if (format.getElements().get(e).getType() == VertexFormatElement.Type.FLOAT) {
                        builder.put(e, sprite.getU(u), sprite.getV(v));
                    } else {//Lightmap UVs (0, 0 is "automatic")
                        builder.put(e, 0, 0);
                    }
                    break;
                case NORMAL:
                    builder.put(e, (float) faceNormal.x(), (float) faceNormal.y(), (float) faceNormal.z());
                    break;
                default:
                    builder.put(e);
            }
    }
}
