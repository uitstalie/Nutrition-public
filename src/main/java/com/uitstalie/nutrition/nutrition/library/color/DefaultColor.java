package com.uitstalie.nutrition.nutrition.library.color;

public class DefaultColor implements MutableColor {
    private int r, g, b, a;

    public DefaultColor(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        validate();
    }

    public static Color of(int r, int g, int b, int a) {
        return new DefaultColor(r, g, b, a);
    }

    public static MutableColor ofMutable(int r, int g, int b, int a) {
        return new DefaultColor(r, g, b, a);
    }

    public static Color of(int r, int g, int b) {
        return of(r, g, b, 255);
    }

    @Override
    public void update(Color color) {
        this.r = color.r();
        this.g = color.g();
        this.b = color.b();
        this.a = color.a();
        validate();
    }

    @Override
    public void update(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        validate();
    }

    @Override
    public void update(String colorString) {
        Color parsed = ColorUtils.parseColorString(colorString);
        this.r = parsed.r();
        this.g = parsed.g();
        this.b = parsed.b();
        this.a = parsed.a();
        validate();
    }

    @Override
    public Color get() {
        return DefaultColor.of(r, g, b, a);
    }

    @Override
    public Integer r() { return r; }

    @Override
    public Integer g() { return g; }

    @Override
    public Integer b() { return b; }

    @Override
    public Integer a() { return a; }

    @Override
    public Double rf() { return 1.0 * r / 255.0; }

    @Override
    public Double gf() { return 1.0 * g / 255.0; }

    @Override
    public Double bf() { return 1.0 * b / 255.0; }

    @Override
    public Double af() { return 1.0 * a / 255.0; }

    @Override
    public String getString() { return ColorUtils.toHexString(this); }

    @Override
    public Integer hex() { return (a << 24) | (r << 16) | (g << 8) | b; }

    private void validate() {
        if (r < 0 || r > 255) throw new IllegalArgumentException("r must be in [0,255]");
        if (g < 0 || g > 255) throw new IllegalArgumentException("g must be in [0,255]");
        if (b < 0 || b > 255) throw new IllegalArgumentException("b must be in [0,255]");
        if (a < 0 || a > 255) throw new IllegalArgumentException("a must be in [0,255]");
    }

    public static Color BLACK = DefaultColor.of(0, 0, 0, 255);
    public static Color WHITE = DefaultColor.of(255, 255, 255, 255);
}
