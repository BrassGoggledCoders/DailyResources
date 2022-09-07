package xyz.brassgoggledcoders.dailyresources.model.barrel;

import com.google.gson.JsonObject;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class TriggerCustomModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    private T model;


    public TriggerCustomModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(TriggerModelLoader.ID, parent, existingFileHelper);
    }

    public void setModel(T model) {
        this.model = model;
    }

    public TriggerCustomModelBuilder<T> withModel(T model) {
        this.model = model;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json.add("model", this.model.toJson());
        return super.toJson(json);
    }
}
