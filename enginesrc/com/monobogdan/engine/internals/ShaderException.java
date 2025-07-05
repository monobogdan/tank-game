package com.monobogdan.engine.internals;

import com.monobogdan.engine.BaseGraphics;
import com.monobogdan.engine.Material;

public class ShaderException extends RuntimeException {

    private static String printArgs(float[] args) {
        StringBuilder ret = new StringBuilder();

        for(int i = 0; i < args.length; i++)
            ret.append(args[i]).append(' ');

        return ret.toString();
    }

    public ShaderException(BaseGraphics.FixedFunctionShader shader, Material material, float[] args, String reason) {
        super(String.format("Shader '%s' thrown exception for material '%s': %s, arguments: %s", shader.getClass().getSimpleName(), material.Name, reason, printArgs(args)));
    }
}
