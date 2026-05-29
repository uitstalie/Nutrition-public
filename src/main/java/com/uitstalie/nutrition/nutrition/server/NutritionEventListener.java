package com.uitstalie.nutrition.nutrition.server;

import com.uitstalie.nutrition.nutrition.Nutrition;
import com.uitstalie.nutrition.nutrition.capabilities.nutrition.NutritionCapability;
import com.uitstalie.nutrition.nutrition.registry.AttributeTypeRegistry;
import com.uitstalie.nutrition.nutrition.service.NutritionFoodService;
import com.uitstalie.nutrition.nutrition.util.log.Log;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 服务端事件监听器。
 *
 * <h3>正常食物</h3>
 * {@link LivingEntityUseItemEvent.Finish} → 从 FoodProperties 提取 healing/saturation，直接 apply。
 *
 * <h3>方块食物（蛋糕/Pie/Feast）</h3>
 * {@link PlayerInteractEvent.RightClickBlock} → 捕获 blockItem 到 Capability，
 * 由 {@code NutritionFoodDataMixin#nutrition$eat} 在 {@code FoodData.eat()} TAIL 消费。
 */
@EventBusSubscriber(modid = Nutrition.MOD_ID)
public class NutritionEventListener {

    // ────── 正常食物 ──────

    @SubscribeEvent
    public static void onUseItemFinish(LivingEntityUseItemEvent.Finish evt) {
        Entity entity = evt.getEntity();
        ItemStack itemStack = evt.getItem();

        if (!(entity instanceof ServerPlayer player)) return;
        if (itemStack.isEmpty()) return;

        // 从物品提取 healing/saturation
        FoodProperties foodProps = itemStack.getFoodProperties(player);
        int healing = foodProps != null ? foodProps.nutrition() : 0;
        float saturation = foodProps != null ? foodProps.saturation() : 0f;

        NutritionFoodService.applyFoodNutrition(player, itemStack, healing, saturation);
    }

    // ────── 方块食物 ──────

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock evt) {
        if (evt.getSide().isClient()) return;
        if (!(evt.getEntity() instanceof ServerPlayer player)) return;

        Block block = evt.getLevel().getBlockState(evt.getPos()).getBlock();
        ItemStack blockItem = new ItemStack(block.asItem());
        if (blockItem.isEmpty()) return;

        NutritionCapability cap = player.getData(AttributeTypeRegistry.NutritionCapability);
        if (cap != null) {
            cap.setCapturedFood(blockItem);
        }
    }
}
