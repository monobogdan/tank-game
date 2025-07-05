package com.monobogdan.engine;

import java.nio.ByteBuffer;

import static android.opengl.GLES11.*;

/**
 * Created by mono on 10.06.2025.
 */

public class Mesh extends BaseMesh {
    private static int[] id = new int[1];

    public Mesh(Runtime runtime, VertexBuffer[] buffers, String name) {
        super(runtime, buffers, name);
    }

    public Mesh(Runtime runtime, String name) {
        super(runtime, name);
    }

    public void bind() {
        if(VertexBufferID == 0)
            throw new RuntimeException("Attempt to bind non-initialized vertex buffer");

        glBindBuffer(GL_ARRAY_BUFFER, VertexBufferID);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IndexBufferID);
    }

    public void upload(ByteBuffer data, ByteBuffer indices) {
        if(VertexBufferID == 0) {
            glGenBuffers(1, id, 0);
            VertexBufferID = id[0];
            glGenBuffers(1, id, 0);
            IndexBufferID = id[0];

            if(VertexBufferID == 0 || IndexBufferID == 0)
                throw new RuntimeException("glGenBuffers failed for VertexBuffer");
        }

        glBindBuffer(GL_ARRAY_BUFFER, VertexBufferID);
        glBufferData(GL_ARRAY_BUFFER, data.capacity(), data, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IndexBufferID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.capacity(), indices, GL_STATIC_DRAW);
    }
}
