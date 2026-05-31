package com.uitstalie.neotrition.capabilities.nutrition;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NutritionCapabilityProvider implements ICapabilityProvider<Player,Void, NutritionCapability> {
    private volatile NutritionCapability nutritionCapability;
    @Override
    public @Nullable NutritionCapability getCapability(@NotNull Player object, Void unused) {
        if(nutritionCapability == null){
            synchronized (this) {
                if(nutritionCapability == null){
                    nutritionCapability = new NutritionCapability();
                }
            }
        }
        return nutritionCapability;
    }
}
