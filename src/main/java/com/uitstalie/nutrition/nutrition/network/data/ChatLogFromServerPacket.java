package com.uitstalie.nutrition.nutrition.network.data;

import com.uitstalie.nutrition.nutrition.Nutrition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ChatLogFromServerPacket(MutableComponent message) implements CustomPacketPayload {
    public static final Type<ChatLogFromServerPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(Nutrition.MOD_ID, "chat_log_from_server_packet")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ChatLogFromServerPacket> CODEC = CustomPacketPayload.codec(
            ChatLogFromServerPacket::encode, ChatLogFromServerPacket::decode
    );

    @Override
    public @NotNull Type<ChatLogFromServerPacket> type() {
        return TYPE;
    }

    public static void encode(ChatLogFromServerPacket packet, RegistryFriendlyByteBuf buf) {
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, packet.message);
    }

    public static ChatLogFromServerPacket decode(RegistryFriendlyByteBuf buf) {
        return new ChatLogFromServerPacket((MutableComponent) ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf));
    }

    public static void handle(ChatLogFromServerPacket message, IPayloadContext context) {
        context.enqueueWork(() -> handle(message));
    }


    private static void handle(ChatLogFromServerPacket packet) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        player.sendSystemMessage(packet.message);
    }

    public static void sendToAll(MutableComponent message) {
        ChatLogFromServerPacket packet = new ChatLogFromServerPacket(message);
        PacketDistributor.sendToAllPlayers(packet);
    }
}
