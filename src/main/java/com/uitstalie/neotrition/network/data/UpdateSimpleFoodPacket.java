package com.uitstalie.neotrition.network.data;

import com.uitstalie.neotrition.Neotrition;
import com.uitstalie.neotrition.capabilities.singleFoodRecordCapability.SingleFoodRecordCapability;
import com.uitstalie.neotrition.registry.AttributeTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record UpdateSimpleFoodPacket(ItemStack itemStack) implements CustomPacketPayload {

    public static final Type<UpdateSimpleFoodPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Neotrition.MOD_ID, "update_simple_food_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateSimpleFoodPacket> CODEC = CustomPacketPayload.codec(
            UpdateSimpleFoodPacket::encode, UpdateSimpleFoodPacket::decode
    );

    @Override
    public @NotNull Type<UpdateSimpleFoodPacket> type() {
        return TYPE;
    }

    public static void encode(UpdateSimpleFoodPacket packet, RegistryFriendlyByteBuf buffer) {
        ItemStack.STREAM_CODEC.encode(buffer, packet.itemStack);
    }

    public static UpdateSimpleFoodPacket decode(RegistryFriendlyByteBuf buffer) {
        return new UpdateSimpleFoodPacket(ItemStack.STREAM_CODEC.decode(buffer));
    }

    public static void handle(UpdateSimpleFoodPacket message, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> handle(message));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void handle(UpdateSimpleFoodPacket packet) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            SingleFoodRecordCapability cap = player.getData(AttributeTypeRegistry.SingleFoodCapability);
            cap.setCurrentEatenFood(packet.itemStack);
        }
    }

    public static void sendTo(ServerPlayer player, String itemId) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId)));
        sendTo(player, stack);
    }

    public static void sendTo(ServerPlayer player, ItemStack item) {
        UpdateSimpleFoodPacket packet = new UpdateSimpleFoodPacket(item);
        PacketDistributor.sendToPlayer(player, packet);
    }
}
