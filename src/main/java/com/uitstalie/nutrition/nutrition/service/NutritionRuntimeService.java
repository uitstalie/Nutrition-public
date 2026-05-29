package com.uitstalie.nutrition.nutrition.service;

import com.uitstalie.nutrition.nutrition.api.data.NutritionDataRegistry;
import com.uitstalie.nutrition.nutrition.api.data.config.NutritionConfigJson;
import com.uitstalie.nutrition.nutrition.capabilities.nutrition.NutritionCapability;
import com.uitstalie.nutrition.nutrition.registry.AttributeTypeRegistry;
import com.uitstalie.nutrition.nutrition.util.effect.NutritionEffectApplier;
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
        }

        NutritionSyncService.syncToClient(player, cap);
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
