package com.uitstalie.neotrition.mixin.common;

import com.uitstalie.neotrition.api.data.NutritionDataRegistry;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into {@link Player#canEat(boolean)}：启用 always-eat 时，
 * 即使饱食度已满也允许进食。
 *
 * <p>仅当 {@code NutritionDataRegistry.isAlwaysEatEnabled()} 返回 true 且
 * vanilla canEat 返回 false 时才介入，将返回值改为 true。</p>
 *
 * <p><b>注入策略</b>：{@code @Inject at RETURN}，只在原始返回 false 时修改。</p>
 */
@Mixin(Player.class)
public abstract class AlwaysEatMixin {

    @Inject(method = "canEat(Z)Z", at = @At("RETURN"), cancellable = true)
    private void nutrition$forceCanEat(boolean flag, CallbackInfoReturnable<Boolean> cir) {
        // 如果 vanilla 已经允许 (true)，不做任何事
        if (cir.getReturnValue()) return;
        // 如果 flag=true，canEat(true) 本来就返回 true，理论上不会到这里
        if (flag) return;
        // 配置关闭则不介入
        if (!NutritionDataRegistry.isAlwaysEatEnabled()) return;

        cir.setReturnValue(true);
    }
}
