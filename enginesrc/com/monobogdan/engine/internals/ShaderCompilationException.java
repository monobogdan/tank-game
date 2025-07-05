package com.monobogdan.engine.internals;

import com.monobogdan.engine.BaseShader;

public class ShaderCompilationException extends RuntimeException {

    public ShaderCompilationException(BaseShader shader, String vertexLog, String pixelLog) {
        super(
                new StringBuilder().append("Failed to compile shader object '").append(shader).append('\'')
                        .append(vertexLog.length() > 0 ? "\nVertex shader log: " : null).append(vertexLog.length() > 0 ? vertexLog : null)
                        .append(pixelLog.length() > 0 ? "\nPixel shader log: " : null).append(pixelLog.length() > 0 ? pixelLog : null).toString()
        );
    }

    public ShaderCompilationException(BaseShader shader, String linkLog) {
        super(
                new StringBuilder().append("Failed to link shader object '").append(shader).append("'\n")
                        .append(linkLog).toString()
        );
    }
}
