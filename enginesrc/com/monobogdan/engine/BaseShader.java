package com.monobogdan.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class BaseShader extends NamedResource {
    public BaseShader(Runtime runtime, String name) {
        super(runtime, name);

    }

    public abstract void compile(String source);
    public abstract void bind();
    public abstract boolean isReady();

    /*public static Shader loadFromFile(Runtime runtime, String fileName) {
        try {
            InputStream strm = runtime.Platform.openFile(fileName);
            Shader ret = loadFromStream(runtime, strm, fileName);
            strm.close();

            return ret;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load shader " + fileName, e);
        }
    }

    public static Shader loadFromStream(Runtime runtime, InputStream strm, String name) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(strm));

        String str;
        StringBuilder builder = new StringBuilder(32768);
        while ((str = reader.readLine()) != null)
            builder.append(str);

        Shader shader = new Shader(runtime, name);
        shader.compile(str);

        reader.close();
        return shader;
    }*/
}
