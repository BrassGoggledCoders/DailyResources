package xyz.brassgoggledcoders.dailyresources.model.fluidtop;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.math.Vector3f;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import xyz.brassgoggledcoders.dailyresources.model.barrel.TriggerModelLoader;

import java.util.ArrayList;
import java.util.List;

public class FluidTopCustomModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    private T model;
    private final List<FluidModelInfo> fluidInfo;


    public FluidTopCustomModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(FluidTopModelLoader.ID, parent, existingFileHelper);
        this.fluidInfo = new ArrayList<>();
    }

    public void setModel(T model) {
        this.model = model;
    }

    public FluidTopCustomModelBuilder<T> withModel(T model) {
        this.setModel(model);
        return this;
    }

    public FluidTopCustomModelBuilder<T> withFluidInfo(FluidModelInfo fluidInfo) {
        this.fluidInfo.add(fluidInfo);
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json.add("model", this.model.toJson());
        JsonArray fluidInfoArray = new JsonArray();
        for (FluidModelInfo modelInfo : fluidInfo) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("from", serializeVector3f(modelInfo.from()));
            jsonObject.add("to", serializeVector3f(modelInfo.to()));
            fluidInfoArray.add(jsonObject);
        }
        json.add("fluidInfo", fluidInfoArray);
        return super.toJson(json);
    }

    private JsonArray serializeVector3f(Vector3f vec) {
        JsonArray ret = new JsonArray();
        ret.add(serializeFloat(vec.x()));
        ret.add(serializeFloat(vec.y()));
        ret.add(serializeFloat(vec.z()));
        return ret;
    }

    private Number serializeFloat(float f) {
        if ((int) f == f) {
            return (int) f;
        }
        return f;
    }
}
