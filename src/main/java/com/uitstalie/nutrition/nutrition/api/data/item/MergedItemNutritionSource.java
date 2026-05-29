package com.uitstalie.nutrition.nutrition.api.data.item;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 按来源优先级合并物品营养绑定。先加入的 source 优先级更高。 */
public class MergedItemNutritionSource implements ItemNutritionSource {

    private final List<ItemNutritionSource> sources = new ArrayList<>();

    public void setSources(List<ItemNutritionSource> sources) {
        this.sources.clear();
        if (sources != null) {
            this.sources.addAll(sources.stream().filter(source -> source != null).toList());
        }
    }

    public void addSource(ItemNutritionSource source) {
        if (source != null) {
            sources.add(source);
        }
    }

    @Override
    public NutritionItemJson getItemConfig(ResourceLocation itemId) {
        if (itemId == null) return null;
        for (ItemNutritionSource source : sources) {
            NutritionItemJson config = source.getItemConfig(itemId);
            if (config != null) return config;
        }
        return null;
    }

    @Override
    public List<NutritionItemJson> getItems() {
        Map<String, NutritionItemJson> merged = new LinkedHashMap<>();
        for (ItemNutritionSource source : sources) {
            for (NutritionItemJson item : source.getItems()) {
                merged.putIfAbsent(item.groups, item);
            }
        }
        return merged.values().stream().toList();
    }
}
