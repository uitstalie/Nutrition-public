package com.uitstalie.nutrition.nutrition.mixin.common;

import com.uitstalie.nutrition.nutrition.api.mixin.PlayerInterface;
import com.uitstalie.nutrition.nutrition.capabilities.nutrition.NutritionCapability;
import com.uitstalie.nutrition.nutrition.registry.AttributeTypeRegistry;
import com.uitstalie.nutrition.nutrition.service.NutritionFoodService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into FoodData.eat(): 方块食物（蛋糕/Pie/Feast）的营养应用入口。
 * <p>
 * 正常食物走 {@code LivingEntityUseItemEvent.Finish} 直接 apply，
 * 方块食物通过 RightClickBlock capture + 此 Mixin 消费。
 * </p>
 */
@Mixin(FoodData.class)
public abstract class NutritionFoodDataMixin implements PlayerInterface {

    @Unique
    Player player_in;

    @Override
    public void setPlayer(Player player) {
        player_in = player;
    }

    @Inject(at = @At("TAIL"), method = "eat(IF)V")
    public void nutrition$eat(int healing, float saturationModifier, CallbackInfo ci) {
        if (player_in == null) return;
        if (player_in.level().isClientSide()) return;
        if (!(player_in instanceof ServerPlayer serverPlayer)) return;

        NutritionCapability cap = serverPlayer.getData(AttributeTypeRegistry.NutritionCapability);
        if (cap == null) return;

        ItemStack food = cap.getCapturedFood();
        if (food.isEmpty()) return;
        cap.clearCapturedFood();

        NutritionFoodService.applyFoodNutrition(serverPlayer, food, healing, saturationModifier);
    }
}
