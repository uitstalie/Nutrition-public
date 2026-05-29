package com.uitstalie.nutrition.nutrition.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.uitstalie.nutrition.nutrition.Nutrition;
import com.uitstalie.nutrition.nutrition.client.ClientNutritionState.SyncedItemNutrition;
import com.uitstalie.nutrition.nutrition.gui.GameGui;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import java.util.List;
import java.util.stream.Collectors;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * 客户端事件监听器。
 */
@EventBusSubscriber(modid = Nutrition.MOD_ID, value = Dist.CLIENT)
public class NutritionClientEventListener {

    /** N 键：打开/关闭营养 GUI */
    public static final KeyMapping OPEN_GUI_KEY = new KeyMapping(
            "key.nutrition.gui",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_N,
            "key.categories.nutrition"
    );

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 将 tick 处理器注册到 NeoForge 游戏事件总线（ClientTickEvent 在 GAME bus 上）
        NeoForge.EVENT_BUS.addListener(NutritionClientEventListener::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(NutritionClientEventListener::onItemTooltip);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_GUI_KEY);
    }

    /**
     * 客户端 tick：检测 N 键按下，打开营养 GUI。
     * 通过 {@link FMLClientSetupEvent} 手动注册到 NeoForge.EVENT_BUS。
     */
    public static void onPlayerTick(ClientTickEvent.Post event) {
        if (OPEN_GUI_KEY.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.screen == null) {
                mc.setScreen(new GameGui());
            }
        }
    }

    /**
     * 物品 tooltip：追加营养绑定信息。
     * <ul>
     *   <li>常显：营养值和 group 名（如 "+520 fruit"）</li>
     *   <li>F3+H 模式：额外显示该物品所属的全部营养组 tag（如 "#nutrition:fruit"）</li>
     * </ul>
     */
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null) return;

        List<SyncedItemNutrition> nutritions = ClientNutritionState.getItemNutritions(itemId);
        List<Component> tooltip = event.getToolTip();

        // 常显：营养值
        if (!nutritions.isEmpty()) {
            for (int i = nutritions.size() - 1; i >= 0; i--) {
                SyncedItemNutrition n = nutritions.get(i);
                int color = ClientNutritionState.getGroupTextColor(n.groupName());
                String text = n.perValue() == -1
                        ? "+" + n.groupName()
                        : "+" + n.perValue() + " " + n.groupName();
                tooltip.add(1, Component.literal(text)
                        .withStyle(style -> style.withColor(color)));
            }
        }

        // F3+H 模式：显示全部营养组 tag（放在营养值行之后）
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.advancedItemTooltips && !nutritions.isEmpty()) {
            String tags = nutritions.stream()
                    .map(n -> "#nutrition:" + n.groupName())
                    .collect(Collectors.joining(" "));
            // 营养值从位置 1 开始插入，共 nutritions.size() 行，
            // tag 行放在营养值之后
            int tagPos = Math.min(1 + nutritions.size(), tooltip.size());
            tooltip.add(tagPos, Component.literal(tags)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
