package com.monobogdan.engine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.EXTBgra.*;
import static org.lwjgl.opengl.EXTTextureCompressionS3TC.*;
import static org.lwjgl.opengl.SGISGenerateMipmap.*;

public class Texture2D extends BaseTexture {
    // Enum index, format, internal format, type
    static final int[] FormatTable = new int[] {
            FORMAT_RGB565, GL_RGB, GL_RGB, GL_UNSIGNED_SHORT_5_6_5,
            FORMAT_RGB, GL_RGB, GL_RGB, GL_UNSIGNED_BYTE,
            FORMAT_RGBA, GL_RGBA, GL_RGBA, GL_UNSIGNED_BYTE
    };

    private Runtime runtime;


    public Texture2D(String name, Runtime runtime) {
        super(name);

        this.runtime = runtime;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

       /* if(ID != 0)
            glDeleteTextures(ID);*/
    }

    public void bind() {
        if(ID == 0)
            throw new RuntimeException("Attempt to bind unassigned texture " + Name);

        glBindTexture(GL_TEXTURE_2D, ID);
    }

    public void setWrapMode(TextureWrap wrap) {
        int glWrap = wrap == TextureWrap.Repeat ? GL_REPEAT : GL_CLAMP_TO_EDGE;

        bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, glWrap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, glWrap);
    }

    // Upload texture with full mipmap-chian generation
    public void upload(ByteBuffer data, int width, int height, int format) {
        int formatTableOffset = -1;

        if(ID == 0) {
            ID = glGenTextures();

            if(ID == 0)
                throw new RuntimeException("glGenTextures failed");

            setWrapMode(TextureWrap.Repeat);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        }

        for(int i = 0; i < FormatTable.length / 4; i++) {
            int idx = FormatTable[i * 4];

            if(idx == format) {
                formatTableOffset = idx;
                break;
            }
        }

        if(formatTableOffset == -1)
            throw new RuntimeException("TextureFormat is not supported ");

        bind();
        glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP_SGIS, GL_TRUE);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
        //MipMapGenerator.generate(this);

        Width = width;
        Height = height;

        SizeInMemory = data.capacity();

        runtime.Platform.log("Texture size is %dKb", SizeInMemory / 1024);
    }
}
