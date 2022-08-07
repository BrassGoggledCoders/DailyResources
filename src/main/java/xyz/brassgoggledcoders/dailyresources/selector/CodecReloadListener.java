package xyz.brassgoggledcoders.dailyresources.selector;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class CodecReloadListener<T> extends SimpleJsonResourceReloadListener {
    private final Logger LOGGER = LoggerFactory.getLogger(CodecReloadListener.class);
    private final Codec<T> codec;
    private final BiMap<ResourceLocation, T> entries;
    private final String path;

    private final IContext context;

    private int generation;

    public CodecReloadListener(Gson gson, String path, Codec<T> codec, IContext context) {
        super(gson, path);
        this.path = path;
        this.codec = codec;
        this.context = context;
        this.entries = HashBiMap.create();
        this.generation = 0;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, @NotNull ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        this.generation++;
        Map<ResourceLocation, T> newEntries = Maps.newHashMap();
        pProfiler.push("Loading from %s".formatted(path));

        for (Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
            String fileName = entry.getKey().toString();
            JsonObject jsonObject = GsonHelper.convertToJsonObject(entry.getValue(), fileName);
            if (CraftingHelper.processConditions(jsonObject, "conditions", context != null ? context : IContext.EMPTY)) {
                codec.decode(JsonOps.INSTANCE, jsonObject)
                        .resultOrPartial(error -> LOGGER.warn(fileName + " failed with error: " + error))
                        .ifPresent(pair -> newEntries.put(entry.getKey(), pair.getFirst()));
            }

        }

        LOGGER.info("Loaded %d entries from %s".formatted(newEntries.size(), this.path));
        entries.clear();
        entries.putAll(newEntries);
        pProfiler.pop();
    }

    public Optional<T> getEntry(ResourceLocation id) {
        return Optional.ofNullable(entries.get(id));
    }

    public Optional<ResourceLocation> getId(T resourceGroup) {
        return Optional.ofNullable(entries.inverse().get(resourceGroup));
    }

    public Stream<Map.Entry<ResourceLocation, T>> getEntries() {
        return entries.entrySet()
                .parallelStream();
    }

    public int getGeneration() {
        return this.generation;
    }
}
