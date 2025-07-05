package com.monobogdan.engine;

public class Material extends NamedResource {
    public static class ShaderInstance {
        public BaseGraphics.FixedFunctionShader Shader;
        public float[] Params;

        public ShaderInstance(BaseGraphics.FixedFunctionShader shader, float[] params) {
            Shader = shader;
            Params = params;
        }
    }
    public static final int COMBINER_STAGE_COUNT = 4; // Default to desktop OpenGL

    public ShaderInstance[] Shaders;
    public Texture2D[] Textures;

    //public Shader Shader;

    //public Texture2D Diffuse;
    //public Texture2D Detail;
    public float R;
    public float G;
    public float B;
    public float A;

    public float AlphaTestValue;

    public boolean DepthWrite;
    public boolean DepthTest;
    public boolean AlphaBlend;
    public boolean AlphaTest;
    public boolean Fog;

    public boolean Unlit;

    public Material(Runtime runtime, String name) {
        super(runtime, name);

        R = 1;
        G = 1;
        B = 1;
        A = 1;

        AlphaTestValue = 0.3f;

        DepthWrite = true;
        DepthTest = true;
        AlphaBlend = false;
        AlphaTest = false;

        Textures = new Texture2D[COMBINER_STAGE_COUNT];
    }

    public static Material createDiffuse(Runtime runtime, String name, Texture2D diffuse) {
        Material material = new Material(runtime, name);
        material.Shaders = new ShaderInstance[] {
           new ShaderInstance(FFPShaders.ShaderHashMap.get("multiply"), new float[] { 0 })
        };
        material.Textures[0] = diffuse;
        //material.Diffuse = diffuse;

        return material;
    }

    public static Material createColor(Runtime runtime, String name, float r, float g, float b, float a) {
        Material material = new Material(runtime, name);
        material.R = r;
        material.G = g;
        material.B = b;
        material.A = a;

        return material;
    }
}
