package com.uitstalie.neotrition.api.data.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.uitstalie.neotrition.api.data.DataPackJsonLoader;
import com.uitstalie.neotrition.util.log.Log;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * 全局配置加载器。
 * 监听 {@code data/neotrition/config/} 目录。
 */
public class NutritionConfigDataListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private NutritionConfigJson config;

    public NutritionConfigDataListener() {
        super(GSON, "config");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        for (var entry : map.entrySet()) {
            try {
                var parsed = NutritionConfigJson.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
                config = parsed.getOrThrow(error -> {
                    Log.e("NutritionConfig", "Failed parsing config: " + entry.getKey() + " — " + error);
                    return new RuntimeException(error);
                });
                Log.d("NutritionConfig", "Loaded config: " + entry.getKey());
                return;
            } catch (Exception e) {
                Log.e("NutritionConfig", "Failed parsing config: " + entry.getKey() + " — " + e.getMessage());
            }
        }
    }

    public NutritionConfigJson getConfig() {
        return config;
    }

    /**
     * 直接从 classpath 加载（绕过 ResourceManager reload）。
     */
    public void loadDirectly(String basePath, String fileName) {
        String fullPath = "/" + basePath + "/" + fileName;
        try {
            JsonElement json = DataPackJsonLoader.loadJson(getClass(), "NutritionConfig", basePath, fileName);
            if (json == null) return;
            config = NutritionConfigJson.CODEC.parse(JsonOps.INSTANCE, json)
                    .getOrThrow(error -> new RuntimeException("Parse error: " + error));
            Log.d("NutritionConfig", "Loaded config directly from: " + fullPath);
        } catch (Exception e) {
            Log.e("NutritionConfig", "Failed loading " + fullPath + " — " + e.getMessage());
        }
    }
}
