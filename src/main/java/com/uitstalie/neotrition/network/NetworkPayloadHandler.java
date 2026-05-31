package com.uitstalie.neotrition.network;

import com.uitstalie.neotrition.network.data.ChatLogFromServerPacket;
import com.uitstalie.neotrition.network.data.NutritionDataSyncPacket;
import com.uitstalie.neotrition.network.data.UpdateSimpleFoodPacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkPayloadHandler {

    public static final String PROTOCOL_VERSION = "1";
    public static PayloadRegistrar INSTANCE;

    public static void register(RegisterPayloadHandlersEvent event) {
        INSTANCE = event.registrar(PROTOCOL_VERSION);
        INSTANCE.playBidirectional(UpdateSimpleFoodPacket.TYPE, UpdateSimpleFoodPacket.CODEC, UpdateSimpleFoodPacket::handle);
        INSTANCE.playBidirectional(ChatLogFromServerPacket.TYPE, ChatLogFromServerPacket.CODEC, ChatLogFromServerPacket::handle);
        INSTANCE.playToClient(NutritionDataSyncPacket.TYPE, NutritionDataSyncPacket.CODEC, NutritionDataSyncPacket::handle);
    }
}
