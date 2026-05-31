package com.uitstalie.neotrition.service;

import com.uitstalie.neotrition.api.data.NutritionDataRegistry;
import com.uitstalie.neotrition.api.data.config.NutritionConfigJson;
import com.uitstalie.neotrition.capabilities.nutrition.NutritionCapability;
import com.uitstalie.neotrition.registry.AttributeTypeRegistry;
import com.uitstalie.neotrition.util.effect.NutritionEffectApplier;
import net.minecraft.server.level.ServerPlayer;

/** 玩家营养运行时应用服务：tick、衰减、effect 刷新和登录同步。 */
public final class NutritionRuntimeService {

    private NutritionRuntimeService() {}

    public static void onSecondEvent(ServerPlayer player) {
        NutritionConfigJson config = NutritionDataRegistry.config();
        if (config == null) return;

        NutritionCapability cap = player.getData(AttributeTypeRegistry.NutritionCapability);
        if (cap == null) return;

        int frequency = config.frequency.toSeconds();
        boolean needSync = false;

        var groups = NutritionDataRegistry.groups();
        if (!groups.isEmpty()) {
            cap.getNutritionData().tickDecay(groups);
        }

        if (config.isFoodRecordEnabled()) {
            cap.getFoodRecord().tick(frequency);
        }

        if (cap.tickEffectRefresh(frequency)) {
            NutritionEffectApplier.refreshAll(
                    player,
                    cap,
                    NutritionDataRegistry.effectsByLocation()
            );
            needSync = true;
        }

        // P1: 仅当营养值发生变化或 effect 刚刷新时才同步
        if (needSync || cap.getNutritionData().isDirty()) {
            NutritionSyncService.syncToClient(player, cap);
            cap.getNutritionData().clearDirty();
        }
    }

    public static void onPlayerLogin(ServerPlayer player) {
        NutritionCapability cap = player.getData(AttributeTypeRegistry.NutritionCapability);
        if (cap == null) return;

        NutritionEffectApplier.refreshAll(
                player,
                cap,
                NutritionDataRegistry.effectsByLocation()
        );

        NutritionSyncService.syncToClient(player, cap);
    }
}
