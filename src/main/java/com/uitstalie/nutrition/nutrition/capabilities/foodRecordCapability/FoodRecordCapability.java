package com.uitstalie.nutrition.nutrition.capabilities.foodRecordCapability;

import net.minecraft.nbt.CompoundTag;

public class FoodRecordCapability {
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

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put("foodRecordManager", FoodRecordManager.serialize(manager));
        tag.putLong("preTick", preTick);
        return tag;
    }

    public void load(CompoundTag compoundTag) {
        if (compoundTag.contains("foodRecordManager")) {
            manager = FoodRecordManager.deserialize(compoundTag.getCompoundOrEmpty("foodRecordManager"));
        }
        if(compoundTag.contains("preTick")){
            preTick = compoundTag.getLongOr("preTick", 0);
        }
    }
}
