package com.uitstalie.nutrition.nutrition.util.effect;

import net.minecraft.resources.Identifier;

/** 当前命中的 effect 领域状态，不依赖网络包 DTO。 */
public record ActiveEffectState(Identifier effectId, int amplifier) {
}
