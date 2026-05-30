package com.uitstalie.nutrition.nutrition.util.log;

import com.uitstalie.nutrition.nutrition.api.data.NutritionDataRegistry;
import com.uitstalie.nutrition.nutrition.network.data.ChatLogFromServerPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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

    public static void send(MutableComponent component) {
        if (!NutritionDataRegistry.isChatLogEnabled()) {
            return;
        }

        MutableComponent message = component.copy();

        if (FMLEnvironment.getDist().isDedicatedServer()) {
            // 专用服务端：通过网络包广播给所有玩家
            ChatLogFromServerPacket.sendToAll(PREFIX_SERVER.copy().append(message));
            return;
        }

        // 集成客户端（或纯客户端）：投递到 render 线程发给本地玩家
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            Minecraft.getInstance().execute(
                    () -> player.sendSystemMessage(PREFIX_CLIENT.copy().append(message)));
        }
    }

    private static LocalPlayer getClientPlayer() {
        return net.minecraft.client.Minecraft.getInstance().player;
    }
}
