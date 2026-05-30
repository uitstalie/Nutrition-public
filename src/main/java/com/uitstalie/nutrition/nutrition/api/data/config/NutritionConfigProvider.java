package com.uitstalie.nutrition.nutrition.api.data.config;

import com.uitstalie.nutrition.nutrition.Nutrition;
import com.uitstalie.nutrition.nutrition.util.data.ValueRange;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.data.JsonCodecProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * DataGen Provider：生成默认 config.json。
 * 输出到 {@code data/nutrition/config/config.json}。
 */
public class NutritionConfigProvider extends JsonCodecProvider<NutritionConfigJson> {

    public NutritionConfigProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, PackOutput.Target.DATA_PACK, "config", NutritionConfigJson.CODEC, lookupProvider, Nutrition.MOD_ID);
    }

    @Override
    protected void gather() {
        var marginalEffect = new NutritionConfigJson.MarginalEffect(
                true, 5,
                List.of(
                        new NutritionConfigJson.MarginalEffect.Rule(new ValueRange(1, 1), 1.0),
                        new NutritionConfigJson.MarginalEffect.Rule(new ValueRange(2, 5), 0.8),
                        new NutritionConfigJson.MarginalEffect.Rule(new ValueRange(6, 10), 0.5)
                )
        );

        NutritionConfigJson defaultConfig = new NutritionConfigJson(
                NutritionConfigJson.Frequency.HIGH,
                true, true, true,
                "healing * 100 + saturation * 50",
                true,
                marginalEffect
        );

        this.unconditional(
                Identifier.fromNamespaceAndPath(Nutrition.MOD_ID, "config"),
                defaultConfig
        );
    }
}
