package com.uitstalie.nutrition.nutrition.util.effect;

import net.minecraft.resources.Identifier;

/** 当前命中的 attribute 领域状态，不依赖网络包 DTO。 */
public record ActiveAttributeState(Identifier attributeId, double amount, String operation) {
}
