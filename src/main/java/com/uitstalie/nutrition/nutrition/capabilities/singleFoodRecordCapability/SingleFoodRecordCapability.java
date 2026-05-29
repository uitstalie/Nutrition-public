package com.uitstalie.nutrition.nutrition.capabilities.singleFoodRecordCapability;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

public class SingleFoodRecordCapability implements INBTSerializable<CompoundTag> {

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

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (!this.currentEatenFood.isEmpty()) {
            tag.put("food", this.currentEatenFood.saveOptional(provider));
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag tag) {
        if (tag.contains("food")) {
            this.currentEatenFood = ItemStack.parseOptional(provider, tag.getCompound("food"));
        }
        else {
            this.currentEatenFood = ItemStack.EMPTY;
        }
    }
}
