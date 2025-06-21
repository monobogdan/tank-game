package com.monobogdan.engine;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

class MeshLoader {
    private static final int HEADER = 0x1234;

    public static Mesh load(Runtime runtime, String fileName) {
        try {
            return load(runtime, runtime.Platform.openFile(fileName), fileName.substring(fileName.lastIndexOf('/') + 1));
        } catch (IOException e) {
            throw new RuntimeException("Mesh not found: " + fileName);
        }
    }

    private static void calculateBoundingBox(BaseMesh.Vertex[] verts) {

    }

    public static Mesh load(Runtime runtime, InputStream strm, String name) {
        if(runtime == null)
            runtime.Platform.log("Runtime can't be null");

        if(name == null)
            runtime.Platform.log("Warning: Attempt to load unnamed mesh");

        if(strm == null)
            throw new NullPointerException("Input stream can't be null for MeshLoader");

        runtime.Platform.log("[Resources] Loading mesh %s", name);

        try {
            DataInputStream inputStream = new DataInputStream(strm);

            int header = inputStream.readInt();
            if(header != HEADER)
                throw new RuntimeException("Not a mesh file");

            int numSubMeshes = inputStream.readInt();

            Mesh.VertexBuffer[] bufs = new Mesh.VertexBuffer[numSubMeshes];

            for(int i = 0; i < numSubMeshes; i++) {
                String subMeshName = inputStream.readUTF();
                int vertexCount = inputStream.readInt();
                int indexCount = inputStream.readInt();

                BaseMesh.Vertex[] vertices = new BaseMesh.Vertex[vertexCount];
                short[] indices = new short[indexCount];

                for(int j = 0; j < vertices.length; j++) {

                    vertices[j] = new BaseMesh.Vertex(inputStream.readFloat(), inputStream.readFloat(), inputStream.readFloat(),
                            inputStream.readFloat(), inputStream.readFloat(), inputStream.readFloat(),
                            inputStream.readFloat(), 1 - inputStream.readFloat());
                }

                for(int j = 0; j < indices.length; j++)
                    indices[j] = inputStream.readShort();

                bufs[i] = new Mesh.VertexBuffer(subMeshName, indices, vertices);
            }

            return new Mesh(runtime, bufs, name);
        } catch (IOException e) {
            runtime.Platform.log("[Resources] Failed to load mesh %s", name);
            runtime.Platform.log(e.getMessage());

            return null;
        }
    }
}
