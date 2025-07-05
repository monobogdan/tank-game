package com.monobogdan.engine.math;

public class Color {
    public static Color White = new Color(1, 1, 1, 1);
    public static Color Black = new Color(0, 0, 0, 1.0f);
    public static Color Blue = new Color(0, 0, 1, 1);

    public float R, G, B, A;

    public Color() {
        R = 1.0f;
        G = 1.0f;
        B = 1.0f;
        A = 1.0f;
    }

    public Color(float r, float g, float b, float a) {
        R = r;
        G = g;
        B = b;
        A = a;
    }
}
