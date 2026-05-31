package com.uitstalie.neotrition.capabilities.foodRecordCapability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FoodRecordManager {
    private final int MAX_RECORD_COUNT = 400;
    private final Map<String,FoodRecord> foodRecords = new LinkedHashMap<>();

    public void reloadRecord(List<FoodRecord> initRecord){
        foodRecords.clear();

        var availableRecord = initRecord.stream()
                .filter(foodRecord -> {
                    ResourceLocation rs = ResourceLocation.tryParse(foodRecord.foodItemId);
                    return rs != null;
                })
                .sorted(Comparator.comparingLong(r -> r.updateTick)); // 确保重载时按时间排序


        availableRecord.forEach(foodRecord -> {
            foodRecords.put(foodRecord.getIndex(),foodRecord);
        });
    }

    public void addRecord(FoodRecord foodRecord){
        if(foodRecords.size() >= MAX_RECORD_COUNT){
            // 删除最早的记录 (LinkedHashMap的第一个元素)
            Iterator<String> it = foodRecords.keySet().iterator();
            if (it.hasNext()) {
                it.next();
                it.remove();
            }
        }
        foodRecords.put(foodRecord.getIndex(),foodRecord);
    }

    public void removeRecordBeforeTick(long tick){
        foodRecords.values().removeIf(value -> value.updateTick < tick);
    }



    public List<FoodRecord> getCurrentRecord(){
        return new ArrayList<>(foodRecords.values());
    }

    public static CompoundTag serialize(FoodRecordManager manager){
        CompoundTag foodRecordManagerTag = new CompoundTag();
        ListTag listTag = new ListTag();

        // 保存为 ListTag，更加标准且紧凑
        for (FoodRecord record : manager.foodRecords.values()) {
            listTag.add(FoodRecord.serialize(record));
        }

        foodRecordManagerTag.put("food_records", listTag);
        return foodRecordManagerTag;
    }

    public static FoodRecordManager deserialize(CompoundTag foodRecordManagerTag){
        FoodRecordManager manager = new FoodRecordManager();
        List<FoodRecord> foodRecordList = new ArrayList<>();

        if (foodRecordManagerTag.contains("food_records", Tag.TAG_LIST)) {
            ListTag listTag = foodRecordManagerTag.getList("food_records", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                FoodRecord record = FoodRecord.deserialize(listTag.getCompound(i));
                if(record != null) {
                    foodRecordList.add(record);
                }
            }
        }
        manager.reloadRecord(foodRecordList);
        return manager;
    }
}
