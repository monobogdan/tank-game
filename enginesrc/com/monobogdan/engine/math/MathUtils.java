package com.monobogdan.engine.math;

public class MathUtils {

    public static float lerp(float a, float b, float f)
    {
        return (a * (1.0f - f)) + (b * f);
    }

    public static float clamp(float val, float min, float max) {
        return val < min ? min : (val > max ? max : val);
    }

    public static float abs(float val) {
        return val < 0 ? -val : val;
    }
}
