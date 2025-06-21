package com.monobogdan.engine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static android.opengl.GLES11.*;

/**
 * Created by mono on 10.06.2025.
 */

public class Texture2D extends BaseTexture {
    public static class MipMapGenerator {
        private static ByteBuffer buf = ByteBuffer.allocateDirect(16 * 6).order(ByteOrder.nativeOrder());

        static {
            buf.putFloat(-1).putFloat(-1).putFloat(0).putFloat(-1);
            buf.putFloat(1).putFloat(-1).putFloat(1).putFloat(-1);
            buf.putFloat(1).putFloat(1).putFloat(1).putFloat(0);
            buf.putFloat(-1).putFloat(-1).putFloat(0).putFloat(-1);
            buf.putFloat(-1).putFloat(1).putFloat(0).putFloat(0);
            buf.putFloat(1).putFloat(1).putFloat(1).putFloat(0);
            buf.rewind();
        }

        public static void generate(Texture2D thiz) {
            int width = thiz.Width;
            int height = thiz.Height;

            int prevWidth = thiz.runtime.Graphics.Viewport.Width;
            int prevHeight = thiz.runtime.Graphics.Viewport.Height;

            // Prepare state
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glDisableClientState(GL_NORMAL_ARRAY);

            // Texture sampler configuration
            thiz.bind();
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            int mip = 1; // First mip is already uploaded

            while(width > 1 && height > 1) {
                width /= 2;
                height /= 2;

                glViewport(0, 0, width, height);

                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                buf.position(0);
                glVertexPointer(2, GL_FLOAT, 16, buf);
                buf.position(8);
                glTexCoordPointer(2, GL_FLOAT, 16, buf);
                glDrawArrays(GL_TRIANGLES, 0, 6);

                glCopyTexImage2D(GL_TEXTURE_2D, mip, GL_RGB, 0, 0, width, height, 0);
                mip++;
            }

            glViewport(0, 0, prevWidth, prevHeight);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        }
    }

    // Enum index, format, internal format, type
    static final int[] FormatTable = new int[] {
            FORMAT_RGB565, GL_RGB, GL_RGB, GL_UNSIGNED_SHORT_5_6_5,
            FORMAT_RGB, GL_RGB, GL_RGB, GL_UNSIGNED_BYTE,
            FORMAT_RGBA, GL_RGBA, GL_RGBA, GL_UNSIGNED_BYTE
    };

    private static int id[] = new int[1];
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
            glGenTextures(1, id, 0);
            ID = id[0];

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
        glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_TRUE);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);

        Width = width;
        Height = height;

        SizeInMemory = data.capacity();

        runtime.Platform.log("Texture size is %dKb", SizeInMemory / 1024);
    }
}
