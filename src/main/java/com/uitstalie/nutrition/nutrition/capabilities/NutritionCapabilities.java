package com.uitstalie.nutrition.nutrition.capabilities;

import com.uitstalie.nutrition.nutrition.Nutrition;
import com.uitstalie.nutrition.nutrition.capabilities.foodRecordCapability.FoodRecordCapability;
import com.uitstalie.nutrition.nutrition.capabilities.nutrition.NutritionCapability;
import com.uitstalie.nutrition.nutrition.capabilities.singleFoodRecordCapability.SingleFoodRecordCapability;
import com.uitstalie.nutrition.nutrition.registry.AttributeTypeRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;


/**
 * capabilities 的总类，负责注册和使用capabilities
 */
public class NutritionCapabilities {
    public static final Identifier NUTRITION = Identifier.fromNamespaceAndPath(Nutrition.MOD_ID,"nutrition");
    public static final Identifier SINGLEFOOD = Identifier.fromNamespaceAndPath(Nutrition.MOD_ID,"singlefood");
    public static final Identifier NUTRITION_FOOD_RECORD = Identifier.fromNamespaceAndPath(Nutrition.MOD_ID,"nutrition_food_record");

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
