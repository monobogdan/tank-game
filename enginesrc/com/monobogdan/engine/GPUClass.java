package com.monobogdan.engine;


public class GPUClass {
    private static class KeyValue {
        public int Key;
        public String Value;

        public KeyValue(String value, int key) {
            Key = key;
            Value = value;
        }
    }

    public static final int QUALITY_LEVEL_SIMPLE = 0;
    public static final int QUALITY_LEVEL_NORMAL = 1;
    public static final int QUALITY_LEVEL_MAXIMUM = 2;

    public String Name;
    public int QualityLevel;
    public boolean HasShaderSupport;

    private static KeyValue[] rendererList = {
        // Mobile GPU
        new KeyValue("videocore iv", QUALITY_LEVEL_SIMPLE),
        new KeyValue("z430", QUALITY_LEVEL_NORMAL),
        new KeyValue("adreno 20", QUALITY_LEVEL_NORMAL),
        new KeyValue("adreno 30", QUALITY_LEVEL_MAXIMUM),
        new KeyValue("sgx 5", QUALITY_LEVEL_MAXIMUM),
        new KeyValue("mali-3", QUALITY_LEVEL_SIMPLE), // Bug in combiner support
        new KeyValue("mali-4", QUALITY_LEVEL_SIMPLE),

        // Desktop GPU
        new KeyValue("mx4", QUALITY_LEVEL_NORMAL),
        new KeyValue("voodoo", QUALITY_LEVEL_NORMAL),
        new KeyValue("radeon 8", QUALITY_LEVEL_NORMAL),
        new KeyValue("radeon 9", QUALITY_LEVEL_MAXIMUM),
        new KeyValue("rage", QUALITY_LEVEL_NORMAL),
        new KeyValue("mirage", QUALITY_LEVEL_NORMAL),
        new KeyValue("unichrome", QUALITY_LEVEL_NORMAL),
        new KeyValue("fx5", QUALITY_LEVEL_MAXIMUM),
        new KeyValue("savage", QUALITY_LEVEL_SIMPLE),
    };

    private GPUClass() { }

    public static GPUClass detect(Runtime.Platform platform, String renderer, String glVersion) {
        GPUClass ret = new GPUClass();

        ret.Name = renderer;
        ret.QualityLevel = QUALITY_LEVEL_MAXIMUM;
        boolean gpuFound = false;

        renderer = renderer.toLowerCase();
        for(int i = 0; i < rendererList.length; i++) {
            if(renderer.contains(rendererList[i].Value)) {
                ret.QualityLevel = rendererList[i].Key;
                gpuFound = true;
                platform.log("Found GPU '%s'. Using %d quality level", rendererList[i].Value, ret.QualityLevel);
            }
        }

        if(!gpuFound)
            platform.log("GPU '%s' is not recognized. Using maximum quality level.", ret.Name);

        if(glVersion.contains("2.") || glVersion.contains("3.") || glVersion.contains("4."))
            ret.HasShaderSupport = true;

        return ret;
    }
}
