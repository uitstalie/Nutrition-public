package com.uitstalie.nutrition.nutrition.api.data.item;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * 物品营养绑定来源。
 *
 * <p>Phase 8 会引入 auto generated 来源；手写 datapack 与自动生成结果都通过该接口暴露，
 * 再由合并层决定优先级。</p>
 */
public interface ItemNutritionSource {

    NutritionItemJson getItemConfig(ResourceLocation itemId);

    List<NutritionItemJson> getItems();
}
