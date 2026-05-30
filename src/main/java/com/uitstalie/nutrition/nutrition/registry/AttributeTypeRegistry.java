package com.uitstalie.nutrition.nutrition.registry;

import com.uitstalie.nutrition.nutrition.Nutrition;
import com.uitstalie.nutrition.nutrition.capabilities.NutritionCapabilities;
import com.uitstalie.nutrition.nutrition.capabilities.foodRecordCapability.FoodRecordCapability;
import com.uitstalie.nutrition.nutrition.capabilities.nutrition.NutritionCapability;
import com.uitstalie.nutrition.nutrition.capabilities.singleFoodRecordCapability.SingleFoodRecordCapability;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.function.Supplier;

public class AttributeTypeRegistry {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Nutrition.MOD_ID);

    public static final Supplier<AttachmentType<NutritionCapability>> NutritionCapability =
        ATTACHMENT_TYPES.register(NutritionCapabilities.NUTRITION.getPath(),
            () -> AttachmentType.builder(NutritionCapability::new)
                .serialize(new IAttachmentSerializer<>() {
                    @Override
                    public NutritionCapability read(IAttachmentHolder holder, ValueInput input) {
                        CompoundTag tag = input.read("data", CompoundTag.CODEC).orElse(new CompoundTag());
                        NutritionCapability cap = new NutritionCapability();
                        cap.load(tag);
                        return cap;
                    }

                    @Override
                    public boolean write(NutritionCapability value, ValueOutput output) {
                        output.store("data", CompoundTag.CODEC, value.save());
                        return true;
                    }
                })
                .build());

    public static final Supplier<AttachmentType<FoodRecordCapability>> FoodRecordCapability =
        ATTACHMENT_TYPES.register(NutritionCapabilities.NUTRITION_FOOD_RECORD.getPath(),
            () -> AttachmentType.builder(FoodRecordCapability::new)
                .serialize(new IAttachmentSerializer<>() {
                    @Override
                    public FoodRecordCapability read(IAttachmentHolder holder, ValueInput input) {
                        CompoundTag tag = input.read("data", CompoundTag.CODEC).orElse(new CompoundTag());
                        FoodRecordCapability cap = new FoodRecordCapability();
                        cap.load(tag);
                        return cap;
                    }

                    @Override
                    public boolean write(FoodRecordCapability value, ValueOutput output) {
                        output.store("data", CompoundTag.CODEC, value.save());
                        return true;
                    }
                })
                .copyOnDeath()
                .build());

    public static final Supplier<AttachmentType<SingleFoodRecordCapability>> SingleFoodCapability =
        ATTACHMENT_TYPES.register(NutritionCapabilities.SINGLEFOOD.getPath(),
            () -> AttachmentType.builder(SingleFoodRecordCapability::new)
                .serialize(new IAttachmentSerializer<>() {
                    @Override
                    public SingleFoodRecordCapability read(IAttachmentHolder holder, ValueInput input) {
                        CompoundTag tag = input.read("data", CompoundTag.CODEC).orElse(new CompoundTag());
                        SingleFoodRecordCapability cap = new SingleFoodRecordCapability();
                        cap.load(tag);
                        return cap;
                    }

                    @Override
                    public boolean write(SingleFoodRecordCapability value, ValueOutput output) {
                        output.store("data", CompoundTag.CODEC, value.save());
                        return true;
                    }
                })
                .build());

    public static void register(IEventBus modBus){
        ATTACHMENT_TYPES.register(modBus);
    }
}
