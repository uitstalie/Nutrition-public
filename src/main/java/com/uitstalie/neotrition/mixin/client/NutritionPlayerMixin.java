package com.uitstalie.neotrition.mixin.client;

import com.uitstalie.neotrition.api.mixin.PlayerInterface;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractClientPlayer.class)
public abstract class NutritionPlayerMixin {

    @Inject(at = @At("RETURN"),method = "<init>*")
    public void nutrition$init(CallbackInfo ci){
        Player player = (Player)(Object)this;
        FoodData foodData = player.getFoodData();
        ((PlayerInterface)foodData).setPlayer(player);
    }
}
