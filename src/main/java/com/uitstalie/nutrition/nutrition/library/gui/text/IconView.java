package com.uitstalie.nutrition.nutrition.library.gui.text;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.uitstalie.nutrition.nutrition.library.gui.base.View;

/**
 * icon view 指的是使用minecraft:items或者其他类似的方式进行的图标显示的view
 */
public class IconView extends View {

    private final Item item;

    public IconView(String itemId) {
        Identifier location = Identifier.parse(itemId);
        item = BuiltInRegistries.ITEM.getValue(location);
        if (item == null) {
            throw new IllegalArgumentException("Item with id " + itemId + " not found");
        }
    }

    @Override
    protected void draw(GuiGraphicsExtractor guiGraphics) {
        guiGraphics.item(new ItemStack(item), x, y);
    }
}
