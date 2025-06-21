package com.monobogdan.engine.tools.utils;

import com.monobogdan.engine.internals.ObjMesh;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

public class ConvertModelTask {
    public static final int HEADER = 0x1234;

    public static class Vertex {
        public float X, Y, Z;
        public float NX, NY, NZ;
        public float U, V;

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

        @Override
        public boolean equals(Object obj) {
            Vertex vertex = (Vertex)obj;

            return X == vertex.X && Y == vertex.Y && Z == vertex.Z && U == vertex.U && V == vertex.V && NX == vertex.NX && NY == vertex.NY && NZ == vertex.NZ;
        }
    }

    private static void buildIndices(ArrayList<Vertex> vertices, ArrayList<Vertex> output, ArrayList<Short> indices) {
        // Find all repeating vertices
        int vOffset = 0;

        for(int i = 0; i < vertices.size(); i++) {
            Vertex vert = vertices.get(i);

            if(!output.contains(vert))
                output.add(vert);

            indices.add((short)output.indexOf(vert));
        }
    }

    public static void convert(InputStream strm, OutputStream outputStream) throws IOException {
        com.monobogdan.engine.internals.ObjMesh mesh = new ObjMesh(strm);

        HashMap<String, ArrayList<Vertex>> meshCollection = new HashMap<String, ArrayList<Vertex>>();

        for(ObjMesh.SubObject subObject : mesh.Objects) {
            for(ObjMesh.Face face : subObject.Faces) {
                if(!meshCollection.containsKey(subObject.Name))
                    meshCollection.put(subObject.Name, new ArrayList<Vertex>());

                for(int i = 0; i < 3; i++) {
                    ObjMesh.Vertex vert = mesh.Vertices.get(face.Vertex[i]);
                    ObjMesh.Normal normal = mesh.Normals.get(face.Normal[i]);
                    ObjMesh.UV uv = mesh.TexCoords.get(face.UV[i]);

                    meshCollection.get(subObject.Name).add(new Vertex(
                            vert.X, vert.Y, vert.Z,
                            normal.X, normal.Y, normal.Z,
                            uv.U, uv.V
                    ));
                }

            }
        }

        System.out.println("Total " + meshCollection.size() + " submeshes");

        /* File format:
            int Header - 0x1234
            int SubMeshCount;

            ...

            struct SubMesh {
                String TextureName;
                int NumVertices;
                int NumIndices;
            }

            ...

            struct Vertex { }
            struct Index { }
        */

        DataOutputStream output = new DataOutputStream(outputStream);

        output.writeInt(HEADER);
        output.writeInt(meshCollection.size());

        ArrayList<Vertex> verts = new ArrayList<Vertex>();
        ArrayList<Short> indices = new ArrayList<Short>();

        // Export SubMesh struct
        for(Map.Entry<String, ArrayList<Vertex>> subMesh : meshCollection.entrySet()) {
            output.writeUTF(subMesh.getKey());

            System.out.println("Building indices");
            buildIndices(subMesh.getValue(), verts, indices);

            output.writeInt(verts.size());
            output.writeInt(indices.size());

            for(Vertex vert : verts) {
                output.writeFloat(vert.X);
                output.writeFloat(vert.Y);
                output.writeFloat(vert.Z);

                output.writeFloat(vert.NX);
                output.writeFloat(vert.NY);
                output.writeFloat(vert.NZ);

                output.writeFloat(vert.U);
                output.writeFloat(vert.V);
            }

            for(Short i : indices)
                output.writeShort(i);

            verts.clear();
            indices.clear();

        }
    }
}
