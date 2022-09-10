package xyz.brassgoggledcoders.dailyresources.model.fluidtop;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Vector3f;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record FluidModelInfo(
        Vector3f from,
        Vector3f to
) {
    public double[] getUVs() {
        return new double[] {
                from().x(),
                from().z(),
                to().x(),
                to().z()
        };
    }

    public static FluidModelInfo[] fromJson(@Nullable JsonElement jsonElement) {
        List<FluidModelInfo> modelInfos = new ArrayList<>();
        if (jsonElement == null || jsonElement.isJsonNull()) {
            throw new JsonParseException("Json Element cannot be null");
        } else if (jsonElement.isJsonArray()) {
            JsonArray jsonElements = jsonElement.getAsJsonArray();
            for (JsonElement arrayElement : jsonElements) {
                modelInfos.addAll(Arrays.asList(fromJson(arrayElement)));
            }
        } else if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            modelInfos.add(new FluidModelInfo(
                    getVector3f(jsonObject, "from"),
                    getVector3f(jsonObject, "to")
            ));

        }
        return modelInfos.toArray(FluidModelInfo[]::new);
    }

    private static Vector3f getVector3f(JsonObject pJson, String pName) {
        JsonArray jsonarray = GsonHelper.getAsJsonArray(pJson, pName);
        if (jsonarray.size() != 3) {
            throw new JsonParseException("Expected 3 " + pName + " values, found: " + jsonarray.size());
        } else {
            float[] afloat = new float[3];

            for (int i = 0; i < afloat.length; ++i) {
                afloat[i] = GsonHelper.convertToFloat(jsonarray.get(i), pName + "[" + i + "]");
            }

            return new Vector3f(afloat[0], afloat[1], afloat[2]);
        }
    }
}
