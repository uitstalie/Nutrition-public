package com.uitstalie.nutrition.nutrition.util.effect;

import net.minecraft.resources.ResourceLocation;

/** 当前命中的 attribute 领域状态，不依赖网络包 DTO。 */
public record ActiveAttributeState(ResourceLocation attributeId, double amount, String operation) {
}
