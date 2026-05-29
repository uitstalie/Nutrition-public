package com.uitstalie.nutrition.nutrition.api.data.group;

import com.uitstalie.nutrition.nutrition.Nutrition;
import com.uitstalie.nutrition.nutrition.library.color.DefaultColor;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.JsonCodecProvider;

import java.util.concurrent.CompletableFuture;

/**
 * DataGen Provider：生成 nutrition group JSON。
 * 输出到 {@code data/nutrition/groups/}。
 */
public class NutritionGroupProvider extends JsonCodecProvider<NutritionGroupJson> {

    public NutritionGroupProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, PackOutput.Target.DATA_PACK, "groups", PackType.SERVER_DATA, NutritionGroupJson.CODEC, lookupProvider, Nutrition.MOD_ID, existingFileHelper);
    }

    private static final int DEFAULT_DECAY_VALUE = 5;
    private static final int DEFAULT_DECAY_FREQUENCY = 3;
    private static final double DEFAULT_DECAY_PRESSURE = 2.0;

    @Override
    protected void gather() {
        add("coffee",     "minecraft:cocoa_beans",      0xFF8B4513);  // brown
        add("eggs",       "minecraft:egg",              0xFFFFFF55);  // yellow
        add("fishs",      "minecraft:cod",              0xFF5588FF);  // blue
        add("fruits",     "minecraft:apple",            0xFFFF5555);  // red
        add("grains",     "minecraft:bread",            0xFFFFCC55);  // golden
        add("honeys",     "minecraft:honey_bottle",     0xFFFFAA00);  // amber
        add("milks",      "minecraft:milk_bucket",      0xFFFFFFFF);  // white
        add("mushrooms",  "minecraft:brown_mushroom",   0xFFCCAA88);  // tan
        add("nuts",       "minecraft:cocoa_beans",      0xFF996633);  // brown
        add("proteins",   "minecraft:cooked_beef",      0xFFFF4444);  // red
        add("salt",       "minecraft:sugar",            0xFFEEEEEE);  // light gray
        add("sugars",     "minecraft:sugar",            0xFFFFBBFF);  // pink
        add("vegetables", "minecraft:carrot",           0xFF55FF55);  // green
        add("wines",           "minecraft:potion",           0xFFAA55FF);  // purple
        add("trace_elements",  "minecraft:iron_ingot",       0xFF8899AA);  // blue-gray
    }

    private void add(String name, String icon, int color) {
        this.unconditional(
                ResourceLocation.fromNamespaceAndPath(Nutrition.MOD_ID, name),
                new NutritionGroupJson(name, icon,
                        DefaultColor.of((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >> 24) & 0xFF),
                        DefaultColor.of((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, (color >> 24) & 0xFF),
                        DEFAULT_DECAY_VALUE, DEFAULT_DECAY_FREQUENCY, DEFAULT_DECAY_PRESSURE, null)
        );
    }
}
