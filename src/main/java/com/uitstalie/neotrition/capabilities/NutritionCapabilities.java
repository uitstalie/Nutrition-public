package com.uitstalie.neotrition.capabilities;

import com.uitstalie.neotrition.Neotrition;
import com.uitstalie.neotrition.capabilities.foodRecordCapability.FoodRecordCapability;
import com.uitstalie.neotrition.capabilities.nutrition.NutritionCapability;
import com.uitstalie.neotrition.capabilities.singleFoodRecordCapability.SingleFoodRecordCapability;
import com.uitstalie.neotrition.registry.AttributeTypeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;


/**
 * capabilities 的总类，负责注册和使用capabilities
 */
public class NutritionCapabilities {
    public static final ResourceLocation NUTRITION = ResourceLocation.fromNamespaceAndPath(Neotrition.MOD_ID,"neotrition");
    public static final ResourceLocation SINGLEFOOD = ResourceLocation.fromNamespaceAndPath(Neotrition.MOD_ID,"singlefood");
    public static final ResourceLocation NUTRITION_FOOD_RECORD = ResourceLocation.fromNamespaceAndPath(Neotrition.MOD_ID,"nutrition_food_record");

    public static final EntityCapability<NutritionCapability,Void> NUTRITION_CAPABILITY = EntityCapability.createVoid(NUTRITION,NutritionCapability.class);
    // 定义 FoodRecord 的 Capability 对象
    public static final EntityCapability<FoodRecordCapability,Void> FOOD_RECORD_CAPABILITY = EntityCapability.createVoid(NUTRITION_FOOD_RECORD, FoodRecordCapability.class);
    public static final EntityCapability<SingleFoodRecordCapability,Void> SIMPLEFOOD_CAPABILITY = EntityCapability.createVoid(SINGLEFOOD, SingleFoodRecordCapability.class);

    public static void attachCapabilityPlayer(RegisterCapabilitiesEvent event){

        event.registerEntity(NUTRITION_CAPABILITY, EntityType.PLAYER, (player, context) -> player.getData(AttributeTypeRegistry.NutritionCapability.get()));

        event.registerEntity(FOOD_RECORD_CAPABILITY, EntityType.PLAYER, (player, context) -> player.getData(AttributeTypeRegistry.FoodRecordCapability.get()));

        event.registerEntity(SIMPLEFOOD_CAPABILITY, EntityType.PLAYER, (player, context) -> player.getData(AttributeTypeRegistry.SingleFoodCapability.get()));
    }

}
