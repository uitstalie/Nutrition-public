package com.uitstalie.neotrition.network.data;

import com.uitstalie.neotrition.Neotrition;
import com.uitstalie.neotrition.client.ClientNutritionState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务端 → 客户端营养数据全量同步包。
 *
 * <p>携带全部 nutrition group 营养值 + 当前活跃的 effect 列表。
 * 登录时全量推送，后续每次 second event 结束后增量推送当前全量。</p>
 */
public record NutritionDataSyncPacket(Map<String, Integer> nutritionValues,
                                      List<EffectEntry> effects,
                                      List<GroupEntry> groups,
                                      List<AttributeEntry> attributes,
                                      List<ItemEntry> itemEntries) implements CustomPacketPayload {

    public static final Type<NutritionDataSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Neotrition.MOD_ID, "nutrition_data_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NutritionDataSyncPacket> CODEC =
            CustomPacketPayload.codec(NutritionDataSyncPacket::encode, NutritionDataSyncPacket::decode);

    @Override
    public @NotNull Type<NutritionDataSyncPacket> type() {
        return TYPE;
    }

    // ── encode / decode ──

    private static void encode(NutritionDataSyncPacket packet, RegistryFriendlyByteBuf buf) {
        Map<String, Integer> nv = packet.nutritionValues;
        buf.writeVarInt(nv.size());
        nv.forEach((k, v) -> {
            buf.writeUtf(k);
            buf.writeVarInt(v);
        });

        List<EffectEntry> ef = packet.effects;
        buf.writeVarInt(ef.size());
        for (EffectEntry e : ef) {
            buf.writeUtf(e.effectId.toString());
            buf.writeVarInt(e.amplifier);
        }

        List<GroupEntry> gr = packet.groups;
        buf.writeVarInt(gr.size());
        for (GroupEntry g : gr) {
            buf.writeUtf(g.groupName);
            buf.writeUtf(g.groupIcon);
            buf.writeVarInt(g.guiTextColor);
            buf.writeVarInt(g.guiPngColor);
        }

        List<AttributeEntry> at = packet.attributes;
        buf.writeVarInt(at.size());
        for (AttributeEntry a : at) {
            buf.writeUtf(a.attributeId.toString());
            buf.writeDouble(a.amount);
            buf.writeUtf(a.operation);
        }

        List<ItemEntry> items = packet.itemEntries;
        buf.writeVarInt(items.size());
        for (ItemEntry item : items) {
            buf.writeUtf(item.itemId);
            buf.writeVarInt(item.nutritions.size());
            for (ItemNutritionEntry n : item.nutritions) {
                buf.writeUtf(n.groupName);
                buf.writeVarInt(n.perValue);
            }
        }
    }

    private static NutritionDataSyncPacket decode(RegistryFriendlyByteBuf buf) {
        int nvSize = buf.readVarInt();
        Map<String, Integer> nv = new LinkedHashMap<>();
        for (int i = 0; i < nvSize; i++) {
            nv.put(buf.readUtf(), buf.readVarInt());
        }

        int efSize = buf.readVarInt();
        List<EffectEntry> ef = new ArrayList<>(efSize);
        for (int i = 0; i < efSize; i++) {
            ResourceLocation id = ResourceLocation.parse(buf.readUtf());
            int amp = buf.readVarInt();
            ef.add(new EffectEntry(id, amp));
        }

        int grSize = buf.readVarInt();
        List<GroupEntry> gr = new ArrayList<>(grSize);
        for (int i = 0; i < grSize; i++) {
            String groupName = buf.readUtf();
            String groupIcon = buf.readUtf();
            int guiTextColor = buf.readVarInt();
            int guiPngColor = buf.readVarInt();
            gr.add(new GroupEntry(groupName, groupIcon, guiTextColor, guiPngColor));
        }

        int atSize = buf.readVarInt();
        List<AttributeEntry> at = new ArrayList<>(atSize);
        for (int i = 0; i < atSize; i++) {
            ResourceLocation attrId = ResourceLocation.parse(buf.readUtf());
            double amount = buf.readDouble();
            String operation = buf.readUtf();
            at.add(new AttributeEntry(attrId, amount, operation));
        }

        int itemSize = buf.readVarInt();
        List<ItemEntry> items = new ArrayList<>(itemSize);
        for (int i = 0; i < itemSize; i++) {
            String itemId = buf.readUtf();
            int nutSize = buf.readVarInt();
            List<ItemNutritionEntry> nutritions = new ArrayList<>(nutSize);
            for (int j = 0; j < nutSize; j++) {
                nutritions.add(new ItemNutritionEntry(buf.readUtf(), buf.readVarInt()));
            }
            items.add(new ItemEntry(itemId, nutritions));
        }

        return new NutritionDataSyncPacket(nv, ef, gr, at, items);
    }

    // ── handle ──

    public static void handle(NutritionDataSyncPacket message, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> handleClient(message));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(NutritionDataSyncPacket packet) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        List<ClientNutritionState.SyncedEffect> se = packet.effects.stream()
                .map(e -> new ClientNutritionState.SyncedEffect(e.effectId, e.amplifier))
                .toList();
        List<ClientNutritionState.SyncedGroup> sg = packet.groups.stream()
                .map(g -> new ClientNutritionState.SyncedGroup(g.groupName, g.groupIcon, g.guiTextColor, g.guiPngColor))
                .toList();
        List<ClientNutritionState.SyncedAttribute> sa = packet.attributes.stream()
                .map(a -> new ClientNutritionState.SyncedAttribute(a.attributeId, a.amount, a.operation))
                .toList();
        List<ClientNutritionState.SyncedItemEntry> si = packet.itemEntries.stream()
                .map(e -> new ClientNutritionState.SyncedItemEntry(
                        ResourceLocation.parse(e.itemId),
                        e.nutritions.stream()
                                .map(n -> new ClientNutritionState.SyncedItemNutrition(n.groupName, n.perValue))
                                .toList()))
                .toList();
        ClientNutritionState.update(packet.nutritionValues, se, sg, sa, si);
    }

    // ── send ──

    /**
     * 向指定玩家推送全量营养数据。
     */
    public static void sendTo(ServerPlayer player,
                               Map<String, Integer> nutritionValues,
                               List<EffectEntry> effects,
                               List<GroupEntry> groups,
                               List<AttributeEntry> attributes,
                               List<ItemEntry> itemEntries) {
        NutritionDataSyncPacket packet = new NutritionDataSyncPacket(
                nutritionValues != null ? new LinkedHashMap<>(nutritionValues) : Map.of(),
                effects != null ? new ArrayList<>(effects) : List.of(),
                groups != null ? new ArrayList<>(groups) : List.of(),
                attributes != null ? new ArrayList<>(attributes) : List.of(),
                itemEntries != null ? new ArrayList<>(itemEntries) : List.of()
        );
        PacketDistributor.sendToPlayer(player, packet);
    }

    /**
     * 同步负载中单条 effect 信息。
     */
    public record EffectEntry(ResourceLocation effectId, int amplifier) {
    }

    /**
     * 同步负载中单条营养组配置信息（group_name / group_icon / 颜色）。
     */
    public record GroupEntry(String groupName, String groupIcon, int guiTextColor, int guiPngColor) {
    }

    /**
     * 同步负载中单条 attribute 信息（attributeId / amount / operation）。
     */
    public record AttributeEntry(ResourceLocation attributeId, double amount, String operation) {
    }

    /**
     * 同步负载中单条物品营养绑定信息（itemId + 营养组列表）。
     */
    public record ItemEntry(String itemId, List<ItemNutritionEntry> nutritions) {
    }

    /**
     * 物品营养绑定中的单条营养组-值对。
     */
    public record ItemNutritionEntry(String groupName, int perValue) {
    }
}
