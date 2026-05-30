package com.uitstalie.nutrition.nutrition.api.data.effect;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.uitstalie.nutrition.nutrition.api.data.DataPackJsonLoader;
import com.uitstalie.nutrition.nutrition.util.log.Log;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Effect 配置加载器。
 * 监听 {@code data/nutrition/effects/} 目录，解析 effect/attribute 规则。
 * 跳过非法条目，不阻塞合法条目加载。
 */
public class NutritionEffectDataListener extends SimpleJsonResourceReloadListener<NutritionEffectJson> {

    private final Map<Identifier, NutritionEffectJson> effects = new LinkedHashMap<>();

    public NutritionEffectDataListener() {
        super(NutritionEffectJson.CODEC, FileToIdConverter.json("effects"));
    }

    @Override
    protected void apply(Map<Identifier, NutritionEffectJson> map, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        effects.clear();

        map.forEach((key, result) -> {
            var validEntries = result.entries.stream()
                    .map(ce -> {
                        var validEffects = ce.effects().stream()
                                .filter(NutritionEffectJson.EffectEntry::isValid).toList();
                        var validAttrs = ce.attributes().stream()
                                .filter(NutritionEffectJson.AttributeEntry::isValid).toList();
                        if (validEffects.isEmpty() && validAttrs.isEmpty()
                                && !(ce.effects().isEmpty() && ce.attributes().isEmpty())) {
                            return null;
                        }
                        return new NutritionEffectJson.CombinedEntry(ce.match(), validEffects, validAttrs);
                    })
                    .filter(ce -> ce != null)
                    .toList();
            NutritionEffectJson sanitized = new NutritionEffectJson(validEntries);

            Log.d("NutritionEffect", "Loaded effect file: " + key
                    + " entries=" + sanitized.entries.size());
            effects.put(key, sanitized);
        });
    }

    public Map<Identifier, NutritionEffectJson> getEffectsWithIdentifier() {
        return effects;
    }

    public List<NutritionEffectJson> getEffects() {
        return effects.values().stream().toList();
    }

    /**
     * 直接从 classpath 加载（绕过 ResourceManager reload）。
     */
    public void loadDirectly(String basePath, String fileName) {
        String fullPath = "/" + basePath + "/" + fileName;
        try {
            JsonElement json = DataPackJsonLoader.loadJson(getClass(), "NutritionEffect", basePath, fileName);
            if (json == null) return;
            var result = NutritionEffectJson.CODEC.parse(JsonOps.INSTANCE, json)
                    .getOrThrow(error -> new RuntimeException("Parse error: " + error));
            NutritionEffectJson sanitized = new NutritionEffectJson(result.entries.stream()
                    .map(ce -> new NutritionEffectJson.CombinedEntry(ce.match(),
                            ce.effects().stream().filter(NutritionEffectJson.EffectEntry::isValid).toList(),
                            ce.attributes().stream().filter(NutritionEffectJson.AttributeEntry::isValid).toList()))
                    .toList());
            Identifier key = Identifier.fromNamespaceAndPath("nutrition",
                    fileName.replace(".json", ""));
            Log.d("NutritionEffect", "Loaded effect directly: " + key);
        } catch (Exception e) {
            Log.e("NutritionEffect", "Failed loading " + fullPath + " — " + e.getMessage());
        }
    }
}
