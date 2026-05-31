package com.uitstalie.neotrition.api.data.group;

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
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 营养组加载器。
 * 监听 {@code data/neotrition/groups/} 目录，解析所有 {@code *.json} 文件。
 * 校验 group_name/group_icon 非空，decay 规则合法性。
 */
public class NutritionGroupDataListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Map<ResourceLocation, NutritionGroupJson> nutritionGroups = new LinkedHashMap<>();

    public NutritionGroupDataListener() {
        super(GSON, "groups");
    }

    /**
     * 直接从 classpath 加载数据包 JSON（绕过 ResourceManager reload）。
     * 用于集成服务端等 reload 不触发的场景。
     *
     * @param basePath 数据包基础路径，如 "data/neotrition/groups"
     * @param fileName JSON 文件名，如 "fruit.json"
     */
    public void loadDirectly(String basePath, String fileName) {
        String fullPath = "/" + basePath + "/" + fileName;
        try {
            JsonElement json = DataPackJsonLoader.loadJson(getClass(), "NutritionGroup", basePath, fileName);
            if (json == null) return;
            var result = NutritionGroupJson.CODEC.parse(JsonOps.INSTANCE, json)
                    .getOrThrow(error -> new RuntimeException("Parse error: " + error));

            ResourceLocation key = ResourceLocation.fromNamespaceAndPath("neotrition",
                    fileName.replace(".json", ""));

            if (result.groupName == null || result.groupName.isBlank()) {
                Log.w("NutritionGroup", "Skipping group with empty group_name: " + key);
                return;
            }
            if (result.groupIcon == null || result.groupIcon.isBlank()) {
                Log.w("NutritionGroup", "Skipping group with empty group_icon: " + key);
                return;
            }
            if (result.decayFrequency <= 0) {
                Log.w("NutritionGroup", "Invalid decay_frequency in group " + key
                        + " (must be >= 1), defaulting to 1");
            }

            Log.d("NutritionGroup", "Loaded group: " + key + " name=" + result.groupName);
            nutritionGroups.put(key, result);
        } catch (Exception e) {
            Log.e("NutritionGroup", "Failed loading " + fullPath + " — " + e.getMessage());
        }
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        nutritionGroups.clear();

        map.forEach((key, json) -> {
            try {
                var parsed = NutritionGroupJson.CODEC.parse(JsonOps.INSTANCE, json);
                var result = parsed.getOrThrow(error -> {
                    Log.e("NutritionGroup", "Failed parsing group json: " + key + " — " + error);
                    return new RuntimeException(error);
                });

                if (!ModList.get().isLoaded(key.getNamespace())) {
                    return;
                }

                // Validate required fields
                if (result.groupName == null || result.groupName.isBlank()) {
                    Log.w("NutritionGroup", "Skipping group with empty group_name: " + key);
                    return;
                }
                if (result.groupIcon == null || result.groupIcon.isBlank()) {
                    Log.w("NutritionGroup", "Skipping group with empty group_icon: " + key);
                    return;
                }

                // Validate decay config
                if (result.decayFrequency <= 0) {
                    Log.w("NutritionGroup", "Invalid decay_frequency in group " + key
                            + " (must be >= 1), defaulting to 1");
                }

                Log.d("NutritionGroup", "Loaded group: " + key + " name=" + result.groupName);
                nutritionGroups.put(key, result);
            } catch (Exception e) {
                Log.e("NutritionGroup", "Failed parsing group json: " + key + " — " + e.getMessage());
            }
        });
    }

    public Map<ResourceLocation, NutritionGroupJson> getNutritionGroupsWithResourceLocation() {
        return nutritionGroups;
    }

    public List<NutritionGroupJson> getNutritionGroups() {
        return nutritionGroups.values().stream().toList();
    }
}
