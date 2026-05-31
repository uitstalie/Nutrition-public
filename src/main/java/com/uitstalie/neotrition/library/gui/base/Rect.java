package com.uitstalie.neotrition.library.gui.base;

/**
 * 一个矩形类，用来定义每个item的位置和占用大小
 */
public class Rect {
    public int x;
    public int y;
    public int w;
    public int h;

    public Rect(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public int centerX() {
        return x + w / 2;
    }

    public int centerY() {
        return y + h / 2;
    }
}
