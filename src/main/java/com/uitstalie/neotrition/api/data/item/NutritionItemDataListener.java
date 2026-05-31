package com.uitstalie.neotrition.api.data.item;

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
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 物品-营养组绑定加载器（group 导向）。
 * 监听 {@code data/neotrition/items/} 目录，每文件为一个营养组的物品列表。
 *
 * <p>内部构建快速查询索引：item → groups、item → group → manualValue。</p>
 */
public class NutritionItemDataListener extends SimpleJsonResourceReloadListener implements ItemNutritionSource {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /** groupName → NutritionItemJson */
    private final Map<String, NutritionItemJson> groupConfigs = new LinkedHashMap<>();
    /** itemId → groupName → manualValue */
    private final Map<String, Map<String, Integer>> manualValues = new HashMap<>();
    /** itemId → Set<groupName> */
    private final Map<String, Set<String>> itemGroups = new HashMap<>();

    public NutritionItemDataListener() {
        super(GSON, "items");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        groupConfigs.clear();
        manualValues.clear();
        itemGroups.clear();

        map.forEach((key, json) -> {
            try {
                var parsed = NutritionItemJson.CODEC.parse(JsonOps.INSTANCE, json);
                var result = parsed.getOrThrow(error -> {
                    Log.e("NutritionItem", "Failed parsing item group: " + key + " — " + error);
                    return new RuntimeException(error);
                });

                if (!result.isValid()) {
                    Log.w("NutritionItem", "Skipping invalid item group: " + key);
                    return;
                }

                String groupName = result.groups;
                Log.d("NutritionItem", "Loaded item group: " + groupName + " with " + result.items.size() + " items");
                groupConfigs.put(groupName, result);

                for (NutritionItemJson.ItemEntry entry : result.items) {
                    // 建 item → groups 索引
                    itemGroups.computeIfAbsent(entry.item(), k -> new HashSet<>()).add(groupName);
                    // 建手动值索引
                    if (entry.hasManualValue()) {
                        manualValues.computeIfAbsent(entry.item(), k -> new HashMap<>())
                                .put(groupName, entry.value());
                    }
                }
            } catch (Exception e) {
                Log.e("NutritionItem", "Failed parsing item group: " + key + " — " + e.getMessage());
            }
        });

        Log.d("NutritionItem", "Loaded " + groupConfigs.size() + " groups with " + itemGroups.size() + " unique items");
    }

    // ────── 查询接口 ──────

    /** 获取指定物品所属的营养组集合。 */
    public Set<String> getGroupsForItem(String itemId) {
        Set<String> groups = itemGroups.get(itemId);
        return groups != null ? groups : Set.of();
    }

    /** 获取物品在指定组中的手动值。未配置返回 null。 */
    @Nullable
    public Integer getManualValue(String itemId, String groupName) {
        Map<String, Integer> itemValues = manualValues.get(itemId);
        return itemValues != null ? itemValues.get(groupName) : null;
    }

    /** 获取指定组的完整配置。 */
    @Nullable
    public NutritionItemJson getGroupConfig(String groupName) {
        return groupConfigs.get(groupName);
    }

    /** 获取所有已加载的 group 配置。 */
    public Map<String, NutritionItemJson> getGroupConfigs() {
        return groupConfigs;
    }

    // ────── ItemNutritionSource 兼容 (deprecated, 保持接口兼容) ──────

    @Override
    @Nullable
    public NutritionItemJson getItemConfig(ResourceLocation itemId) {
        // 不再支持 per-item 查询，返回 null
        return null;
    }

    @Override
    public List<NutritionItemJson> getItems() {
        return groupConfigs.values().stream().toList();
    }

    // ────── classpath 直接加载 ──────

    public void loadDirectly(String basePath, String fileName) {
        String fullPath = "/" + basePath + "/" + fileName;
        try {
            JsonElement json = DataPackJsonLoader.loadJson(getClass(), "NutritionItem", basePath, fileName);
            if (json == null) return;
            var result = NutritionItemJson.CODEC.parse(JsonOps.INSTANCE, json)
                    .getOrThrow(error -> new RuntimeException("Parse error: " + error));
            if (!result.isValid()) { Log.w("NutritionItem", "Skipping invalid: " + fullPath); return; }

            String groupName = result.groups;
            groupConfigs.put(groupName, result);
            for (NutritionItemJson.ItemEntry entry : result.items) {
                itemGroups.computeIfAbsent(entry.item(), k -> new HashSet<>()).add(groupName);
                if (entry.hasManualValue()) {
                    manualValues.computeIfAbsent(entry.item(), k -> new HashMap<>())
                            .put(groupName, entry.value());
                }
            }
            Log.d("NutritionItem", "Loaded item group directly: " + groupName + " (" + result.items.size() + " items)");
        } catch (Exception e) {
            Log.e("NutritionItem", "Failed loading " + fullPath + " — " + e.getMessage());
        }
    }
}
