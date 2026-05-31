package com.uitstalie.neotrition.util.data;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

/**
 * 闭区间 [below, above]。
 *
 * <p>统一用于营养系统中的区间判定：effect 匹配（{@code Prediction}）、
 * 衰减规则（{@code DecayRule}）、边际效应规则（{@code marginal_effect.rules}）。</p>
 *
 * <p>提供 {@link #contains(int)} 进行区间归属判断，替代各处手写的
 * {@code value >= below && value <= above}。</p>
 *
 * @param below 区间下界（inclusive）
 * @param above 区间上界（inclusive）
 */
public record ValueRange(int below, int above) {

    /**
     * 检查 value 是否落在 [below, above] 闭区间内。
     * 纯逻辑判定，不做合法性检查。
     *
     * @param value 待检查值
     * @return true 当且仅当 below <= value <= above
     */
    public boolean contains(int value) {
        return value >= below && value <= above;
    }

    /**
     * 基础合法性检查：上界 >= 下界。
     * 各调用方可自行追加额外约束（如 below >= 0、below >= 1 等）。
     *
     * @return true 当且仅当 above >= below
     */
    public boolean isValid() {
        return above >= below;
    }

    /**
     * 在列表中查找第一个 range 包含 value 的元素的索引。
     * 跳过 {@code isValid() == false} 的条目。
     *
     * @param items      待搜索列表
     * @param rangeFn    从元素提取 ValueRange 的函数
     * @param value      要检查的值
     * @return 第一个命中元素的索引，未命中返回 {@code OptionalInt.empty()}
     */
    public static <T> OptionalInt findFirstIndex(List<T> items,
                                                   Function<T, ValueRange> rangeFn,
                                                   int value) {
        for (int i = 0; i < items.size(); i++) {
            ValueRange range = rangeFn.apply(items.get(i));
            if (!range.isValid()) continue;
            if (range.contains(value)) return OptionalInt.of(i);
        }
        return OptionalInt.empty();
    }

    /**
     * 在列表中查找第一个 range 包含 value 的元素。
     * 跳过 {@code isValid() == false} 的条目。
     *
     * @param items      待搜索列表
     * @param rangeFn    从元素提取 ValueRange 的函数
     * @param value      要检查的值
     * @return 第一个命中元素，未命中返回 {@code Optional.empty()}
     */
    public static <T> Optional<T> findFirst(List<T> items,
                                              Function<T, ValueRange> rangeFn,
                                              int value) {
        for (T item : items) {
            ValueRange range = rangeFn.apply(item);
            if (!range.isValid()) continue;
            if (range.contains(value)) return Optional.of(item);
        }
        return Optional.empty();
    }
}
