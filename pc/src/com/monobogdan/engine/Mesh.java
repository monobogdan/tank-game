package com.monobogdan.engine;

import com.monobogdan.engine.BaseMesh;
import com.monobogdan.engine.Runtime;
import com.monobogdan.engine.math.Vector;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glClientActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class Mesh extends BaseMesh {
    public Mesh(Runtime runtime, VertexBuffer[] buffers, String name) {
        super(runtime, buffers, name);
    }

    public Mesh(Runtime runtime, String name) {
        super(runtime, name);
    }

    ByteBuffer VertexData;
    ByteBuffer IndexData;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        if(VertexBufferID != 0)
            glDeleteBuffers(VertexBufferID);

        if(IndexBufferID != 0)
            glDeleteBuffers(IndexBufferID);
    }

    void bind(int offset) {
        int bufferPath = Runtime.Graphics.BufferPath;

        if((bufferPath == Graphics.BUFFER_PATH_VERTEX_POINTERS && (VertexData == null || IndexData == null)) ||
                (bufferPath == Graphics.BUFFER_PATH_VBO && (VertexBufferID == 0 || IndexBufferID == 0)))
            throw new UnsupportedOperationException("Attempt to bind uninitialized buffer " + Name);

        if(bufferPath == Graphics.BUFFER_PATH_VBO) {
            glBindBuffer(GL_ARRAY_BUFFER, VertexBufferID);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IndexBufferID);

            int baseOffset = (BaseMesh.Vertex.Size * offset);

            for (int i = 0; i < Material.COMBINER_STAGE_COUNT; i++) {
                glClientActiveTexture(GL_TEXTURE0 + i);
                glEnableClientState(GL_TEXTURE_COORD_ARRAY);
                glTexCoordPointer(2, GL_FLOAT, BaseMesh.Vertex.Size, baseOffset + 24);
            }

            glVertexPointer(3, GL_FLOAT, BaseMesh.Vertex.Size, baseOffset);
            glNormalPointer(GL_FLOAT, BaseMesh.Vertex.Size, baseOffset + 12);
        } else {
            int baseOffset = (BaseMesh.Vertex.Size * offset);
            VertexData.position(baseOffset + 24);

            for (int i = 0; i < Material.COMBINER_STAGE_COUNT; i++) {
                glClientActiveTexture(GL_TEXTURE0 + i);
                glEnableClientState(GL_TEXTURE_COORD_ARRAY);
                glTexCoordPointer(2, GL_FLOAT, BaseMesh.Vertex.Size, VertexData);
            }

            VertexData.position(baseOffset);
            glVertexPointer(3, GL_FLOAT, BaseMesh.Vertex.Size, VertexData);
            VertexData.position(baseOffset + 12);
            glNormalPointer(GL_FLOAT, BaseMesh.Vertex.Size, VertexData);

            VertexData.rewind();
        }
    }

    public void upload(ByteBuffer data, ByteBuffer indices) {
        if(Runtime.Graphics.BufferPath == Graphics.BUFFER_PATH_VBO) {
            if (VertexBufferID == 0) {
                VertexBufferID = glGenBuffers();
                IndexBufferID = glGenBuffers();

                if (VertexBufferID == 0 || IndexBufferID == 0)
                    throw new RuntimeException("glGenBuffers failed for VertexBuffer");
            }

            glBindBuffer(GL_ARRAY_BUFFER, VertexBufferID);
            glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, IndexBufferID);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
        }

        if(Runtime.Graphics.BufferPath == Graphics.BUFFER_PATH_VERTEX_POINTERS) {
            VertexData = data;
            IndexData = indices;
        }
    }
}
