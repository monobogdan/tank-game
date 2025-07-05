package com.monobogdan.engine;

import com.monobogdan.engine.internals.ShaderCompilationException;

import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.*;

public class Shader extends BaseShader {
    private int program, vertexShader, pixelShader;
    private boolean isReady;

    public Shader(Runtime runtime, String name) {
        super(runtime, name);

        program = glCreateProgram();

        if(program == 0)
            throw new RuntimeException("Failed to create shader program for " + this);

        vertexShader = glCreateShader(GL_VERTEX_SHADER);
        pixelShader = glCreateShader(GL_FRAGMENT_SHADER);

        if(vertexShader == 0)
            throw new RuntimeException("Failed to create vertex shader for " + this);

        if(pixelShader == 0)
            throw new RuntimeException("Failed to create pixel shader for " + this);
    }

    private String preprocessShader(String sourceCode, boolean isVertex) {
        StringBuilder shaderCode = new StringBuilder(4096);

        if(isVertex)
            shaderCode.append("#define VERTEX\n");
        shaderCode.append(sourceCode);

        return shaderCode.toString();
    }

    @Override
    public void compile(String source) {
        Runtime.Platform.log("Compiling shader '%s'", toString());

        isReady = false;

        glShaderSource(vertexShader, preprocessShader(source, true));
        glShaderSource(pixelShader, preprocessShader(source, false));

        glCompileShader(vertexShader);
        glCompileShader(pixelShader);

        String vertexShaderLog = glGetShaderInfoLog(vertexShader, 4096);
        String pixelShaderLog = glGetShaderInfoLog(pixelShader, 4096);

        if(glGetShaderi(vertexShader, GL_COMPILE_STATUS) != GL_TRUE || glGetShaderi(pixelShader, GL_COMPILE_STATUS) != GL_TRUE)
            throw new ShaderCompilationException(this, vertexShaderLog, pixelShaderLog);

        glAttachShader(program, vertexShader);
        glAttachShader(program, pixelShader);

        glLinkProgram(program);

        if(glGetProgrami(program, GL_LINK_STATUS) != GL_TRUE)
            throw new ShaderCompilationException(this, glGetProgramInfoLog(program, 4096));

        isReady = true;
    }

    @Override
    public void bind() {
        if(isReady)
            glUseProgram(program);
        else
            throw new RuntimeException("Attempt to bind unready shader object " + this);
    }

    @Override
    public boolean isReady() {
        return this.isReady;
    }
}
