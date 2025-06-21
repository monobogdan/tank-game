package com.monobogdan.engine;

import com.monobogdan.engine.math.BoundingBox;
import com.monobogdan.engine.math.Vector;

import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.WeakHashMap;

public abstract class BaseMesh {
    public static class Vertex {
        public float X;
        public float Y;
        public float Z;

        public float NX;
        public float NY;
        public float NZ;

        public float U;
        public float V;

        public static final int Size = 32;

        public Vertex(float x, float y, float z, float nx, float ny, float nz, float u, float v) {
            X = x;
            Y = y;
            Z = z;

            NX = nx;
            NY = ny;
            NZ = nz;

            U = u;
            V = v;
        }

        public Vertex(float x, float y, float z, float u, float v) {
            X = x;
            Y = y;
            Z = z;

            U = u;
            V = v;
        }

        public Vertex setPosition(float x, float y, float z) {
            X = x;
            Y = y;
            Z = z;

            return this;
        }

        public Vertex setUV(float u, float v) {
            U = u;
            V = v;

            return this;
        }
    }

    public static class VertexBuffer {
        public String Name;
        public Vertex[] Vertices;
        public short[] Indices;

        public VertexBuffer(String name, short[] indices, Vertex... vertices) {
            Name = name;
            Vertices = vertices;
            Indices = indices;
        }
    }

    public class TriangleList {
        public int Offset;
        public int VertexBufferOffset;
        public int Count;
    }

    public Vector BoundingMin = new Vector(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
    public Vector BoundingMax = new Vector(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

    public int VertexBufferID;
    public int IndexBufferID;

    public HashMap<String, TriangleList> TriangleLists;

    public BaseMesh(Runtime runtime, VertexBuffer[] buffers, String name) {
        if(buffers == null)
            throw new RuntimeException("VertexBuffer is null for mesh " + name);

        TriangleLists = new HashMap<String, TriangleList>();
        int offset = 0;
        int indexOffset = 0;

        for(int i = 0; i < buffers.length; i++) {
            TriangleList list = new TriangleList();
            list.Offset = indexOffset;
            list.Count = buffers[i].Indices.length;
            list.VertexBufferOffset = offset;
            TriangleLists.put(buffers[i].Name, list);

            offset += buffers[i].Vertices.length;
            indexOffset += buffers[i].Indices.length;
        }

        // Convert to FloatBuffer
        final ByteBuffer buf = ByteBuffer.allocateDirect(Vertex.Size * offset);
        buf.order(ByteOrder.nativeOrder());

        final ByteBuffer indices = ByteBuffer.allocateDirect(2 * indexOffset).order(ByteOrder.nativeOrder());

        for(int i = 0; i < buffers.length; i++) {
            for(int j = 0; j < buffers[i].Vertices.length; j++) {
                // Calculate bounding box while uploading geometry
                BoundingMin.X = Math.min(BoundingMin.X, buffers[i].Vertices[j].X);
                BoundingMin.Y = Math.min(BoundingMin.Y, buffers[i].Vertices[j].Y);
                BoundingMin.Z = Math.min(BoundingMin.Z, buffers[i].Vertices[j].Z);

                BoundingMax.X = Math.max(BoundingMax.X, buffers[i].Vertices[j].X);
                BoundingMax.Y = Math.max(BoundingMax.Y, buffers[i].Vertices[j].Y);
                BoundingMax.Z = Math.max(BoundingMax.Z, buffers[i].Vertices[j].Z);

                // Put vertex data into buffer
                buf.putFloat(buffers[i].Vertices[j].X);
                buf.putFloat(buffers[i].Vertices[j].Y);
                buf.putFloat(buffers[i].Vertices[j].Z);

                buf.putFloat(buffers[i].Vertices[j].NX);
                buf.putFloat(buffers[i].Vertices[j].NY);
                buf.putFloat(buffers[i].Vertices[j].NZ);

                buf.putFloat(buffers[i].Vertices[j].U);
                buf.putFloat(buffers[i].Vertices[j].V);
            }

            for(int j = 0; j < buffers[i].Indices.length; j++)
                indices.putShort(buffers[i].Indices[j]);
        }

        buf.rewind();
        indices.rewind();

        runtime.Scheduler.runOnMainThreadIfNeeded(new Runnable() {
            @Override
            public void run() {
                upload(buf, indices);
            }
        });
    }

    protected abstract void upload(ByteBuffer buf, ByteBuffer indices);
}
