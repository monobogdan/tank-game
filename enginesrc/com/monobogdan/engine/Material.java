package com.monobogdan.engine;

public class Material {
    public String Name;

    public Texture2D Diffuse;
    public Texture2D Detail;
    public float R;
    public float G;
    public float B;
    public float A;

    public float AlphaTestValue;

    public boolean DepthWrite;
    public boolean DepthTest;
    public boolean AlphaBlend;
    public boolean AlphaTest;

    public boolean Unlit;

    public Material(String name) {
        R = 1;
        G = 1;
        B = 1;
        A = 1;

        AlphaTestValue = 0.3f;

        DepthWrite = true;
        DepthTest = true;
        AlphaBlend = false;
        AlphaTest = false;
    }

    public static Material createDiffuse(String name, Texture2D diffuse) {
        Material material = new Material(name);
        material.Diffuse = diffuse;

        return material;
    }

    public static Material createColor(String name, float r, float g, float b, float a) {
        Material material = new Material(name);
        material.R = r;
        material.G = g;
        material.B = b;
        material.A = a;

        return material;
    }

    @Override
    public String toString() {
        return Name + hashCode();
    }
}
