package com.uitstalie.nutrition.nutrition.api.data.item;

import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * 基于 item tag 的营养物品来源（0.0.3 废弃逻辑）。
 *
 * <p>0.0.3 tag 化后，group 归属直接通过 ItemStack.getTags() 查询，
 * 不再走 NutritionItemJson 通道。此 source 保留但始终返回空。</p>
 */
public class TagItemNutritionSource implements ItemNutritionSource {

    @Override
    public NutritionItemJson getItemConfig(Identifier itemId) {
        return null;
    }

    @Override
    public List<NutritionItemJson> getItems() {
        return List.of();
    }
}
