package xyz.brassgoggledcoders.dailyresources.model.barrel;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import org.jetbrains.annotations.NotNull;
import xyz.brassgoggledcoders.dailyresources.DailyResources;

import javax.annotation.ParametersAreNonnullByDefault;

public class TriggerModelLoader implements IGeometryLoader<TriggerModel> {
    public static final String ID = "trigger";

    @Override
    @NotNull
    @ParametersAreNonnullByDefault
    public TriggerModel read(JsonObject modelContents, JsonDeserializationContext deserializationContext) {
        return new TriggerModel(
                deserializationContext.deserialize(modelContents.get("model"), BlockModel.class)
        );
    }
}
