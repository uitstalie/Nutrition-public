package com.uitstalie.nutrition.nutrition.library.gui.text;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.uitstalie.nutrition.nutrition.library.gui.base.View;

/**
 * icon view 指的是使用minecraft:items或者其他类似的方式进行的图标显示的view
 */
public class IconView extends View {

    private final Item item;

    public IconView(String itemId) {
        ResourceLocation location = ResourceLocation.parse(itemId);
        item = BuiltInRegistries.ITEM.get(location);
        if (item == null) {
            throw new IllegalArgumentException("Item with id " + itemId + " not found");
        }
    }

    @Override
    protected void draw(GuiGraphics guiGraphics) {
        guiGraphics.renderItem(new ItemStack(item), x, y);
    }
}
