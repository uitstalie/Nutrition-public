package com.uitstalie.neotrition.library.gui.base;

import net.minecraft.client.gui.GuiGraphics;

public class View extends Rect {
    public View() {
        this(0, 0);
    }

    public View(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    public View(int x, int y) {
        this(x, y, 0, 0);
    }

    public void moveTo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    public void resize(int w, int h) {
        this.w = w;
        this.h = h;
    }

    public void stretch(int dw, int dh) {
        this.w += dw;
        this.h += dh;
    }

    protected void draw(GuiGraphics guiGraphics) {
        // Override to implement custom drawing logic
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.pose().pushPose();
        draw(guiGraphics);
        guiGraphics.pose().popPose();
    }
}
