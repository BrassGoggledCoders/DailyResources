package xyz.brassgoggledcoders.dailyresources.model.fluidtop;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelLoader;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.dailyresources.DailyResources;

import javax.annotation.ParametersAreNonnullByDefault;

public class FluidTopModelLoader implements IModelLoader<FluidTopModel> {
    public static final ResourceLocation ID = DailyResources.rl("fluid_top");

    @Override
    @NotNull
    @ParametersAreNonnullByDefault
    public FluidTopModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        return new FluidTopModel(
                deserializationContext.deserialize(modelContents.get("model"), BlockModel.class),
                FluidModelInfo.fromJson(modelContents.get("fluidInfo"))
        );
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager pResourceManager) {

    }
}
