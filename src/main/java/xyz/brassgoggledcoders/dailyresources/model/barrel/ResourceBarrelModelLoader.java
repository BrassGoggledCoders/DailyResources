package xyz.brassgoggledcoders.dailyresources.model.barrel;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelLoader;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

public class ResourceBarrelModelLoader implements IModelLoader<ResourceBarrelModel> {
    @Override
    @NotNull
    @ParametersAreNonnullByDefault
    public ResourceBarrelModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        return new ResourceBarrelModel(
                deserializationContext.deserialize(modelContents.get("model"), BlockModel.class)
        );
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager pResourceManager) {

    }
}
