package com.uitstalie.nutrition.nutrition.capabilities.foodRecordCapability;

import com.uitstalie.nutrition.nutrition.capabilities.foodRecordCapability.exception.FoodRecordException;
import com.uitstalie.nutrition.nutrition.util.log.Log;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

public class FoodRecord {

    String foodItemId;
    long updateTick; //time: level.getGameTime可以获得当前的游戏时

    private String index;

    public static class InvalidFoodRecordException extends RuntimeException {
        public InvalidFoodRecordException(FoodRecordException cause) {
            super(cause);
        }
    }

    public FoodRecord(){
        foodItemId = "";
        updateTick = 0L;
        index = "";
    }

    public String getIndex() {
        return index;
    }

    public void validate() throws FoodRecordException {
        if(foodItemId.isEmpty()){
            throw new FoodRecordException("foodItemId cannot be empty");
        }

        if(updateTick == 0L){
            throw new FoodRecordException("updateTick cannot be 0");
        }

        if(index.isEmpty()){
            throw new FoodRecordException("index cannot be empty");
        }
        if(!index.equals(foodItemId + "_" + updateTick)){
            throw new FoodRecordException("index must be foodItemId + \"_\" + updateTick");
        }
    }

    public FoodRecord(String foodItemId, long updateTick){
        this.foodItemId = foodItemId;
        this.updateTick = updateTick;
        this.index = foodItemId + "_" + updateTick;
    }

    public static CompoundTag serialize(FoodRecord foodRecord) {
        CompoundTag tag = new CompoundTag();
        tag.putString("food_item_id", foodRecord.foodItemId);
        tag.putLong("update_tick",foodRecord.updateTick);
        tag.putString("index",foodRecord.index);
        return tag;
    }

    public static @Nullable FoodRecord deserialize(CompoundTag tag, boolean validate){
        FoodRecord foodRecord = new FoodRecord();
        if(tag.contains("food_item_id")){
            foodRecord.foodItemId = tag.getStringOr("food_item_id", "");
        }
        if(tag.contains("update_tick")){
            foodRecord.updateTick = tag.getLongOr("update_tick", 0);
        }
        if(tag.contains("index")){
            foodRecord.index = tag.getStringOr("index", "");
        }

        try{
            foodRecord.validate();
        } catch (FoodRecordException e) {
            if(validate){
                throw new InvalidFoodRecordException(e);
            }
            else{
                Log.e("FoodRecord", "Invalid FoodRecord data: " + e.getMessage());
                return null;
            }
        }
        return foodRecord;
    }

    public static @Nullable FoodRecord deserialize(CompoundTag tag){
        return deserialize(tag,false);
    }
}
