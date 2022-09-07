package xyz.brassgoggledcoders.dailyresources.model.barrel;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelLoader;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.dailyresources.DailyResources;

import javax.annotation.ParametersAreNonnullByDefault;

public class TriggerModelLoader implements IModelLoader<TriggerModel> {
    public static final ResourceLocation ID = DailyResources.rl("trigger");
    @Override
    @NotNull
    @ParametersAreNonnullByDefault
    public TriggerModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        return new TriggerModel(
                deserializationContext.deserialize(modelContents.get("model"), BlockModel.class)
        );
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager pResourceManager) {

    }
}
