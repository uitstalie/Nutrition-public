package com.uitstalie.nutrition.nutrition.library.color;

/**
 * Color,不可变接口
 */
public interface Color {

    Color get();

    Integer r();
    Integer g();
    Integer b();
    Integer a();

    Double rf();
    Double gf();
    Double bf();
    Double af();

    String getString();
    Integer hex();
}
