package com.uitstalie.nutrition.nutrition.util.log;

import com.uitstalie.nutrition.nutrition.api.data.NutritionDataRegistry;
import com.uitstalie.nutrition.nutrition.network.data.ChatLogFromServerPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.Map;

public class ChatLog {
    private static final String tag = "ChatLog";
    private static final MutableComponent PREFIX = ChatLogBuilder.buildFromTextStyles(Map.of(
            "Nutrition_ChatLog: ", Style.EMPTY.withColor(ChatFormatting.GREEN).withBold(true)
    ));

    private static final MutableComponent PREFIX_ERROR = ChatLogBuilder.buildFromTextStyles(Map.of(
            "Nutrition_ChatLog: ", Style.EMPTY.withColor(ChatFormatting.RED).withBold(true)
    ));

    private static final MutableComponent PREFIX_CLIENT = ChatLogBuilder.buildFromTextStyles(Map.of(
            "[Client] ", Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(true)
    ));

    private static final MutableComponent PREFIX_SERVER = ChatLogBuilder.buildFromTextStyles(Map.of(
            "[Server] ", Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(true)
    ));

    public static void send(MutableComponent component){

        if(!NutritionDataRegistry.isChatLogEnabled()){
            return;
        }

        MutableComponent message = component.copy();
        if (FMLEnvironment.dist.isClient()) {
            LocalPlayer player = getClientPlayer();
            if (player != null && player.level().isClientSide()) {
                player.sendSystemMessage(PREFIX_CLIENT.copy().append(message));
            }
            return;
        }

        ChatLogFromServerPacket.sendToAll(PREFIX_SERVER.copy().append(message));

    }

    @net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
    private static LocalPlayer getClientPlayer() {
        return net.minecraft.client.Minecraft.getInstance().player;
    }
}
