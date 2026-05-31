package com.uitstalie.neotrition.util.effect;

import net.minecraft.resources.ResourceLocation;

/** 当前命中的 effect 领域状态，不依赖网络包 DTO。 */
public record ActiveEffectState(ResourceLocation effectId, int amplifier) {
}
