package com.uitstalie.nutrition.nutrition.capabilities.singleFoodRecordCapability;

import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

public class SingleFoodRecordCapability {

    private ItemStack currentEatenFood;

    public SingleFoodRecordCapability() {
        // 使用 ItemStack.EMPTY 初始化，避免 null
        currentEatenFood = ItemStack.EMPTY;
    }

    public void setCurrentEatenFood(ItemStack currentEatenFood) {
        this.currentEatenFood = currentEatenFood == null ? ItemStack.EMPTY : currentEatenFood;
    }

    // 修正：Getter 不需要参数
    public ItemStack getCurrentEatenFood() {
        return currentEatenFood;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (!this.currentEatenFood.isEmpty()) {
            DataResult<net.minecraft.nbt.Tag> result = ItemStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, this.currentEatenFood);
            result.result().ifPresent(t -> tag.put("food", t));
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        if (tag.contains("food")) {
            this.currentEatenFood = ItemStack.OPTIONAL_CODEC.parse(NbtOps.INSTANCE, tag.get("food"))
                .result().orElse(ItemStack.EMPTY);
        } else {
            this.currentEatenFood = ItemStack.EMPTY;
        }
    }
}
