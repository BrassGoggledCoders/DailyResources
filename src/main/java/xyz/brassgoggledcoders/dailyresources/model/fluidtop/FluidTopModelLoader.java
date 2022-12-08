package xyz.brassgoggledcoders.dailyresources.model.fluidtop;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

public class FluidTopModelLoader implements IGeometryLoader<FluidTopModel> {
    public static final String ID = "fluid_top";

    @Override
    @NotNull
    @ParametersAreNonnullByDefault
    public FluidTopModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        return new FluidTopModel(
                deserializationContext.deserialize(jsonObject.get("model"), BlockModel.class),
                FluidModelInfo.fromJson(jsonObject.get("fluidInfo"))
        );
    }
}
