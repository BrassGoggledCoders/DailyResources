package xyz.brassgoggledcoders.dailyresources.selector;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CodecDataProvider<T> implements DataProvider {
    private final String name;
    private final String modId;
    private final String path;
    private final Codec<T> codec;
    private final DataGenerator dataGenerator;
    private final Map<String, T> entries;

    public CodecDataProvider(String name, String modId, String path, Codec<T> codec, DataGenerator dataGenerator) {
        this.name = name;
        this.modId = modId;
        this.path = path;
        this.codec = codec;
        this.dataGenerator = dataGenerator;
        this.entries = Maps.newHashMap();
    }

    public void add(String name, T entry) {
        this.entries.put(name, entry);
    }

    public CodecDataProvider<T> with(String name, T entry) {
        this.add(name, entry);
        return this;
    }

    @Override
    public void run(@NotNull CachedOutput pCache) throws IOException {
        Path path = this.dataGenerator.getOutputFolder();
        Set<ResourceLocation> set = Sets.newHashSet();
        for (Entry<String, T> entry : this.entries.entrySet()) {
            ResourceLocation id = new ResourceLocation(this.modId, entry.getKey());
            if (!set.add(id)) {
                throw new IllegalStateException("Duplicate %s: %s".formatted(this.path, id));
            } else {
                DataProvider.saveStable(
                        pCache,
                        this.codec.encodeStart(JsonOps.INSTANCE, entry.getValue())
                                .getOrThrow(false, s -> {
                                }),
                        path
                );
            }
        }
    }

    @Override
    @NotNull
    public String getName() {
        return this.name;
    }
}
