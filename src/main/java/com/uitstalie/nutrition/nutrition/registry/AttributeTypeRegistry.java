package com.uitstalie.nutrition.nutrition.registry;

import com.uitstalie.nutrition.nutrition.Nutrition;
import com.uitstalie.nutrition.nutrition.capabilities.NutritionCapabilities;
import com.uitstalie.nutrition.nutrition.capabilities.foodRecordCapability.FoodRecordCapability;
import com.uitstalie.nutrition.nutrition.capabilities.nutrition.NutritionCapability;
import com.uitstalie.nutrition.nutrition.capabilities.singleFoodRecordCapability.SingleFoodRecordCapability;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class AttributeTypeRegistry {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Nutrition.MOD_ID);
    public static final Supplier<AttachmentType<NutritionCapability>> NutritionCapability = register(NutritionCapabilities.NUTRITION, NutritionCapability::new);
    public static final Supplier<AttachmentType<FoodRecordCapability>> FoodRecordCapability = registerAndRestoreWhenDeath(NutritionCapabilities.NUTRITION_FOOD_RECORD, FoodRecordCapability::new);
    public static final Supplier<AttachmentType<SingleFoodRecordCapability>> SingleFoodCapability = register(NutritionCapabilities.SINGLEFOOD, SingleFoodRecordCapability::new);


    private static <S extends Tag,T extends INBTSerializable<S>> Supplier<AttachmentType<T>> register(
            ResourceLocation name,Supplier<T> deufaultValueSupplier
    ){
        return ATTACHMENT_TYPES.register(name.getPath(),()-> AttachmentType.serializable(deufaultValueSupplier)
                .build());
    }

    private static <S extends Tag,T extends INBTSerializable<S>> Supplier<AttachmentType<T>> registerAndRestoreWhenDeath(
            ResourceLocation name,Supplier<T> deufaultValueSupplier
    ){
        return ATTACHMENT_TYPES.register(name.getPath(),()-> AttachmentType.serializable(deufaultValueSupplier)
                .copyOnDeath()
                .build());
    }

    public static void register(IEventBus modBus){
        ATTACHMENT_TYPES.register(modBus);
    }
}
