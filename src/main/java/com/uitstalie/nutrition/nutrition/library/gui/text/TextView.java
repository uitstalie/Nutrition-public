package com.uitstalie.nutrition.nutrition.library.gui.text;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.uitstalie.nutrition.nutrition.library.color.Color;
import com.uitstalie.nutrition.nutrition.library.color.ColorUtils;
import com.uitstalie.nutrition.nutrition.library.color.DefaultColor;
import com.uitstalie.nutrition.nutrition.library.gui.base.View;

public class TextView extends View {

    private final Component text;
    private boolean isCenter = false;
    private Color color = DefaultColor.WHITE;

    private TextView(Component text) {
        this.text = text;
    }

    public void center(boolean isCenter) {
        this.isCenter = isCenter;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setColor(String colorString) {
        this.color = ColorUtils.parseColorString(colorString);
    }

    public void setColor(int r, int g, int b) {
        this.color = DefaultColor.of(r, g, b);
    }

    public void setColor(int r, int g, int b, int a) {
        this.color = DefaultColor.of(r, g, b, a);
    }

    public static TextView ofLiteral(String text) {
        return new TextView(Component.literal(text));
    }

    public static TextView ofTranslatable(String textID) {
        return new TextView(Component.translatable(textID));
    }

    @Override
    protected void draw(GuiGraphics guiGraphics) {
        PoseStack pose = guiGraphics.pose();
        pose.translate(x, y, 0);

        if (isCenter) {
            guiGraphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    text,
                    0,
                    0,
                    color.hex()
            );
        } else {
            guiGraphics.drawString(
                    Minecraft.getInstance().font,
                    text,
                    0,
                    0,
                    color.hex()
            );
        }
    }
}
