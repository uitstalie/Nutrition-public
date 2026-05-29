package com.uitstalie.nutrition.nutrition.library.color;

import com.mojang.serialization.Codec;

public class ColorUtils {

    public static final Codec<Color> COLOR_CODEC = Codec.STRING.xmap(ColorUtils::parseColorString, ColorUtils::toHexString);

    /**
     * 十六进制颜色正则表达式
     * 格式 #AARRGGBB 或者#RRGGBB
     */
    public static String PATTERN_HEX = "^#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?$";

    public static Color parseColorString(String colorString) {
        if (colorString == null) {
            throw new IllegalArgumentException("Color string cannot be null");
        }
        colorString = colorString.trim();
        if (colorString.matches(PATTERN_HEX)) {
            int r, g, b, a;
            if (colorString.length() == 7) {
                // #RRGGBB
                r = Integer.parseInt(colorString.substring(1, 3), 16);
                g = Integer.parseInt(colorString.substring(3, 5), 16);
                b = Integer.parseInt(colorString.substring(5, 7), 16);
                a = 255;
            } else {
                // #AARRGGBB
                a = Integer.parseInt(colorString.substring(1, 3), 16);
                r = Integer.parseInt(colorString.substring(3, 5), 16);
                g = Integer.parseInt(colorString.substring(5, 7), 16);
                b = Integer.parseInt(colorString.substring(7, 9), 16);
            }
            return DefaultColor.of(r, g, b, a);
        } else {
            throw new IllegalArgumentException("Unsupported color format: " + colorString);
        }
    }

    public static MutableColor parseColor(String colorString) {
        return (MutableColor) parseColorString(colorString);
    }

    public static String toHexString(Color color) {
        int r = color.r();
        int g = color.g();
        int b = color.b();
        int a = color.a();
        return String.format("#%02X%02X%02X%02X", a, r, g, b);
    }
}
