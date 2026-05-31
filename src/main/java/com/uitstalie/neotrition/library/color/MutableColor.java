package com.uitstalie.neotrition.library.color;

/**
 * Color,可变接口
 */
public interface MutableColor extends Color {
    void update(Color color);
    void update(int r, int g, int b, int a);
    void update(String colorString);
}
