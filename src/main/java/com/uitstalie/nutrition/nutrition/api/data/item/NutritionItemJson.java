package com.uitstalie.nutrition.nutrition.api.data.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * 营养组物品绑定配置（data/nutrition/items/{group_name}.json）。
 *
 * <p>一文件管一组：声明组名，列出该组下所有物品，每项可附带手动营养值（覆盖公式）。
 * 不写 value 的物品走公式折算。Group 成员关系由此文件完全确定，不再依赖 item tag。</p>
 *
 * <h3>JSON 结构</h3>
 * <pre>{@code
 * {
 *   "groups": "fruit",
 *   "items": [
 *     { "item": "minecraft:apple", "value": 3000 },
 *     { "item": "minecraft:melon_slice" }
 *   ]
 * }
 * }</pre>
 */
public class NutritionItemJson {

    /** 营养组名（对应 groups/ 下的 group_name）。 */
    public final String groups;
    /** 该组下的物品列表。 */
    public final List<ItemEntry> items;

    public NutritionItemJson(String groups, List<ItemEntry> items) {
        this.groups = groups;
        this.items = items;
    }

    public static final Codec<NutritionItemJson> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("groups").forGetter(c -> c.groups),
            Codec.list(ItemEntry.CODEC).fieldOf("items").forGetter(c -> c.items)
    ).apply(instance, NutritionItemJson::new));

    /**
     * 单个物品项。
     *
     * @param item  物品 ID（如 minecraft:apple）
     * @param value 手动营养值，null 表示走公式
     */
    public record ItemEntry(String item, @Nullable Integer value) {

        public static final Codec<ItemEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("item").forGetter(ItemEntry::item),
                Codec.INT.optionalFieldOf("value").forGetter(e -> Optional.ofNullable(e.value))
        ).apply(instance, (item, valueOpt) -> new ItemEntry(item, valueOpt.orElse(null))));

        public boolean hasManualValue() {
            return value != null;
        }
    }

    /**
     * 查找指定物品在该组中的手动值。
     *
     * @return 手动值，未配置返回 null
     */
    @Nullable
    public Integer getManualValue(String itemId) {
        for (ItemEntry entry : items) {
            if (entry.item().equals(itemId) && entry.hasManualValue()) {
                return entry.value();
            }
        }
        return null;
    }

    /** 检查该组是否包含指定物品。 */
    public boolean containsItem(String itemId) {
        for (ItemEntry entry : items) {
            if (entry.item().equals(itemId)) return true;
        }
        return false;
    }

    public boolean isValid() {
        return groups != null && !groups.isBlank()
                && items != null && !items.isEmpty();
    }
}
