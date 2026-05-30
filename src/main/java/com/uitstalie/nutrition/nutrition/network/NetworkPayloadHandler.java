package com.uitstalie.nutrition.nutrition.network;

import com.uitstalie.nutrition.nutrition.network.data.ChatLogFromServerPacket;
import com.uitstalie.nutrition.nutrition.network.data.NutritionDataSyncPacket;
import com.uitstalie.nutrition.nutrition.network.data.UpdateSimpleFoodPacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkPayloadHandler {

    public static final String PROTOCOL_VERSION = "1";
    public static PayloadRegistrar INSTANCE;

    public static void register(RegisterPayloadHandlersEvent event) {
        INSTANCE = event.registrar(PROTOCOL_VERSION);
        INSTANCE.playToClient(UpdateSimpleFoodPacket.TYPE, UpdateSimpleFoodPacket.CODEC, UpdateSimpleFoodPacket::handle);
        INSTANCE.playToClient(ChatLogFromServerPacket.TYPE, ChatLogFromServerPacket.CODEC, ChatLogFromServerPacket::handle);
        INSTANCE.playToClient(NutritionDataSyncPacket.TYPE, NutritionDataSyncPacket.CODEC, NutritionDataSyncPacket::handle);
    }
}
