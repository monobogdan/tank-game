package com.monobogdan.engine;

import com.monobogdan.engine.internals.ShaderException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

import static android.opengl.GLES11.*;

/**
 * Created by mono on 29.06.2025.
 */

public class FFPShaders {
    private static FloatBuffer tmpBuffer = ByteBuffer.allocateDirect(4 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

    static class Sample implements BaseGraphics.FixedFunctionShader {

        @Override
        public void onApply(Material material, int combiner, float[] params) {
            if(params.length != 1)
                throw new ShaderException(this, material, params, "Expected 1 argument");

            int texId = (int)params[0];
            Texture2D tex = material.Textures[texId];

            if(tex == null)
                throw new ShaderException(this, material, params, "Texture " + texId + " was null");

            tex.bind();

            glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_RGB, GL_REPLACE);
            glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_ALPHA, GL_REPLACE);
            glTexEnvi(GL_TEXTURE_ENV, GL_SRC0_RGB, GL_TEXTURE0 + combiner);
            glTexEnvi(GL_TEXTURE_ENV, GL_SRC0_ALPHA, GL_TEXTURE0 + combiner);

            glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_RGB, GL_SRC_COLOR);
            glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_ALPHA, GL_SRC_ALPHA);
        }
    }

    static class Multiply implements BaseGraphics.FixedFunctionShader {

        @Override
        public void onApply(Material material, int combiner, float[] params) {
            if(params.length != 1)
                throw new ShaderException(this, material, params, "Expected 1 argument");

            int texId = (int)params[0];
            Texture2D tex = material.Textures[texId];

            if(tex == null)
                throw new ShaderException(this, material, params, "Texture " + texId + " was null");

            tex.bind();

            glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_RGB, GL_MODULATE);
            glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_ALPHA, GL_MODULATE);
            glTexEnvi(GL_TEXTURE_ENV, GL_SRC0_RGB, GL_PREVIOUS);
            glTexEnvi(GL_TEXTURE_ENV, GL_SRC0_ALPHA, GL_PREVIOUS);
            glTexEnvi(GL_TEXTURE_ENV, GL_SRC1_RGB, GL_TEXTURE0 + combiner);
            glTexEnvi(GL_TEXTURE_ENV, GL_SRC1_ALPHA, GL_TEXTURE0 + combiner);

            glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_RGB, GL_SRC_COLOR);
            glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_ALPHA, GL_SRC_ALPHA);
            glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND1_RGB, GL_SRC_COLOR);
            glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND1_ALPHA, GL_SRC_ALPHA);
        }
    }

    static class Interpolate implements BaseGraphics.FixedFunctionShader {

        @Override
        public void onApply(Material material, int combiner, float[] params) {
            if(params.length != 2)
                throw new ShaderException(this, material, params, "Expected 1 argument");

            int texId = (int)params[0];
            if(material.Textures[texId] == null)
                throw new ShaderException(this, material, params, "Texture " + texId + " was null");

            material.Textures[texId].bind();

            // Interpolation factor
            float factor = params[1];
            tmpBuffer.put(0).put(0).put(0).put(factor);
            tmpBuffer.rewind();

            glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_RGB, GL_INTERPOLATE);
            glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_ALPHA, GL_MODULATE);
            glTexEnvi(GL_TEXTURE_ENV, GL_SRC0_RGB, GL_PREVIOUS);
            glTexEnvi(GL_TEXTURE_ENV, GL_SRC0_ALPHA, GL_PREVIOUS);
            glTexEnvi(GL_TEXTURE_ENV, GL_SRC1_RGB, GL_TEXTURE0 + combiner );
            glTexEnvi(GL_TEXTURE_ENV, GL_SRC1_ALPHA, GL_TEXTURE0 + combiner);
            glTexEnvi(GL_TEXTURE_ENV, GL_SRC2_RGB, GL_CONSTANT);
            glTexEnvi(GL_TEXTURE_ENV, GL_SRC2_ALPHA, GL_CONSTANT);
            glTexEnvfv(GL_TEXTURE_ENV, GL_TEXTURE_ENV_COLOR, tmpBuffer);

            glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_RGB, GL_SRC_COLOR);
            glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_ALPHA, GL_SRC_ALPHA);
            glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND1_RGB, GL_SRC_COLOR);
            glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND1_ALPHA, GL_SRC_ALPHA);
            glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND2_RGB, GL_SRC_ALPHA);
            glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND2_ALPHA, GL_SRC_ALPHA);
        }
    }

    static class MultiplyColor implements BaseGraphics.FixedFunctionShader {

        @Override
        public void onApply(Material material, int combiner, float[] params) {
            int texId = (int)params[0];
            if(material.Textures[texId] == null)
                throw new ShaderException(this, material, params, "Texture " + texId + " was null");

            material.Textures[texId].bind();

            glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_RGB, GL_MODULATE);
            glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_ALPHA, GL_MODULATE);
            glTexEnvi(GL_TEXTURE_ENV, GL_SRC0_RGB, GL_PREVIOUS);
            glTexEnvi(GL_TEXTURE_ENV, GL_SRC0_ALPHA, GL_PREVIOUS);
            glTexEnvi(GL_TEXTURE_ENV, GL_SRC1_RGB, GL_PRIMARY_COLOR);
            glTexEnvi(GL_TEXTURE_ENV, GL_SRC1_ALPHA, GL_PRIMARY_COLOR);

            glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_RGB, GL_SRC_COLOR);
            glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_ALPHA, GL_SRC_ALPHA);
            glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND1_RGB, GL_SRC_COLOR);
            glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND1_ALPHA, GL_SRC_ALPHA);
        }
    }

    public static BaseGraphics.FixedFunctionShader[] Shaders = {
            new Multiply(),
            new Interpolate(),
            new Sample(),
            new MultiplyColor()
    };
    public static HashMap<String, BaseGraphics.FixedFunctionShader> ShaderHashMap = new HashMap<String, BaseGraphics.FixedFunctionShader>(Shaders.length);

    static {
        // Build hashmap for faster search
        for(int i = 0; i < Shaders.length; i++)
            ShaderHashMap.put(Shaders[i].getClass().getSimpleName().toLowerCase(), Shaders[i]);
    }
}
