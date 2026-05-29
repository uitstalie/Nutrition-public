package com.uitstalie.nutrition.nutrition.capabilities.foodRecordCapability;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

public class FoodRecordCapability implements INBTSerializable<CompoundTag> {
    public static final long FOOD_RECORD_EXPIRE_TICK = 60 * 5 * 20L;//5分钟过期，20tick/s 所以是60s*5*20tick/s

    private FoodRecordManager manager = new FoodRecordManager();

    public FoodRecordCapability(){}
    private long preTick = 0L;

    public FoodRecordManager getManager() {
        return manager;
    }

    public void setPreTick(long preTick){
        this.preTick = preTick;
    }

    public void onPlayerTick(long currentTick){
        if(currentTick - preTick > FOOD_RECORD_EXPIRE_TICK){
            manager.removeRecordBeforeTick(currentTick);
            preTick = currentTick;
        }
    }

    public void addRecord(String itemId,long tick){
        FoodRecord record = new FoodRecord(itemId,tick);
        manager.addRecord(record);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("foodRecordManager", FoodRecordManager.serialize(manager));
        tag.putLong("preTick", preTick);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag compoundTag) {
        if (compoundTag.contains("foodRecordManager")) {
            manager = FoodRecordManager.deserialize(compoundTag.getCompound("foodRecordManager"));
        }
        if(compoundTag.contains("preTick")){
            preTick = compoundTag.getLong("preTick");
        }
    }
}
