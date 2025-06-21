package com.monobogdan.engine;

enum TextureWrap {
    Clamp,
    Repeat,
    Unknown;
}

public class BaseTexture {
    public static final int FORMAT_RGB = 0;
    public static final int FORMAT_RGBA = 1;
    public static final int FORMAT_RGB565 = 2;

    public int ID;
    public int SizeInMemory;

    public int Width;
    public int Height;

    public String Name;

    public BaseTexture(String name) {
        Name = name == null ? "Unnamed texture " + hashCode() : name;
    }
}
