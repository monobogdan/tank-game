package com.monobogdan.engine;

import com.monobogdan.engine.math.Vector;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class Mesh extends BaseMesh {
    public Mesh(Runtime runtime, VertexBuffer[] buffers, String name) {
        super(runtime, buffers, name);
    }

    public void bind() {
        if(VertexBufferID == 0)
            throw new RuntimeException("Attempt to bind non-initialized vertex buffer");

        glBindBuffer(GL_ARRAY_BUFFER, VertexBufferID);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IndexBufferID);
    }

    public void upload(ByteBuffer data, ByteBuffer indices) {
        if(VertexBufferID == 0) {
            VertexBufferID = glGenBuffers();
            IndexBufferID = glGenBuffers();

            if(VertexBufferID == 0 || IndexBufferID == 0)
                throw new RuntimeException("glGenBuffers failed for VertexBuffer");
        }

        glBindBuffer(GL_ARRAY_BUFFER, VertexBufferID);
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IndexBufferID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
    }
}
