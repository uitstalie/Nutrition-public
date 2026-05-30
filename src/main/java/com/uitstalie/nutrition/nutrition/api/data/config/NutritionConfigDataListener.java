package com.uitstalie.nutrition.nutrition.api.data.config;

import com.uitstalie.nutrition.nutrition.api.data.DataPackJsonLoader;
import com.uitstalie.nutrition.nutrition.util.log.Log;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonElement;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * 全局配置加载器。
 * 监听 {@code data/nutrition/config/} 目录。
 */
public class NutritionConfigDataListener extends SimpleJsonResourceReloadListener<NutritionConfigJson> {

    private NutritionConfigJson config;

    public NutritionConfigDataListener() {
        super(NutritionConfigJson.CODEC, FileToIdConverter.json("config"));
    }

    @Override
    protected void apply(Map<Identifier, NutritionConfigJson> map, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        for (var entry : map.entrySet()) {
            config = entry.getValue();
            Log.d("NutritionConfig", "Loaded config: " + entry.getKey());
            return;
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
