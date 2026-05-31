package com.uitstalie.neotrition.client;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户端侧营养数据持有。
 *
 * <p>接收服务端同步的营养值、effect/attribute 状态和 nutrition group 配置，供 GUI 展示。
 * 数据以服务端为权威源，客户端只读不写。</p>
 *
 * <h3>线程</h3>
 * 所有访问仅在客户端主线程（网络包 handle 在 enqueueWork 中执行）。
 */
public final class ClientNutritionState {

    private static final Map<String, Integer> nutritionValues = new LinkedHashMap<>();
    private static List<SyncedEffect> effects = List.of();
    private static List<SyncedGroup> groups = List.of();
    private static List<SyncedAttribute> attributes = List.of();
    private static final Map<ResourceLocation, List<SyncedItemNutrition>> itemNutritions = new LinkedHashMap<>();

    private ClientNutritionState() {
    }

    /**
     * 服务端推送的全量数据更新。
     */
    public static void update(Map<String, Integer> values,
                               List<SyncedEffect> effects,
                               List<SyncedGroup> groups,
                               List<SyncedAttribute> attributes,
                               List<SyncedItemEntry> items) {
        nutritionValues.clear();
        nutritionValues.putAll(values);
        ClientNutritionState.effects = effects != null ? List.copyOf(effects) : List.of();
        ClientNutritionState.groups = groups != null ? List.copyOf(groups) : List.of();
        ClientNutritionState.attributes = attributes != null ? List.copyOf(attributes) : List.of();
        itemNutritions.clear();
        if (items != null) {
            for (SyncedItemEntry entry : items) {
                itemNutritions.put(entry.itemId, List.copyOf(entry.nutritions));
            }
        }
    }

    public static Map<String, Integer> getNutritionValues() {
        return Collections.unmodifiableMap(nutritionValues);
    }

    public static int getNutrition(String groupName) {
        return nutritionValues.getOrDefault(groupName, 0);
    }

    public static List<SyncedEffect> getEffects() {
        return effects;
    }

    public static List<SyncedGroup> getGroups() {
        return groups;
    }

    public static List<SyncedAttribute> getAttributes() {
        return attributes;
    }

    /** 查询物品的营养绑定，未配置返回空列表。 */
    public static List<SyncedItemNutrition> getItemNutritions(ResourceLocation itemId) {
        return itemNutritions.getOrDefault(itemId, List.of());
    }

    /** 按 groupName 查找组的文字颜色，未找到返回白色。 */
    public static int getGroupTextColor(String groupName) {
        for (SyncedGroup g : groups) {
            if (g.groupName.equals(groupName)) {
                return g.guiTextColor;
            }
        }
        return 0xFFFFFF;
    }

    /**
     * 一条已同步的 effect 信息。
     */
    public record SyncedEffect(ResourceLocation effectId, int amplifier) {
    }

    /**
     * 一条已同步的营养组配置信息。
     */
    public record SyncedGroup(String groupName, String groupIcon, int guiTextColor, int guiPngColor) {
    }

    /**
     * 一条已同步的 attribute 信息。
     */
    public record SyncedAttribute(ResourceLocation attributeId, double amount, String operation) {
    }

    /**
     * 一条已同步的物品营养绑定（物品 ID + 营养组列表）。
     */
    public record SyncedItemEntry(ResourceLocation itemId, List<SyncedItemNutrition> nutritions) {
    }

    /**
     * 物品营养绑定中的单条营养组-值对。
     */
    public record SyncedItemNutrition(String groupName, int perValue) {
    }
}