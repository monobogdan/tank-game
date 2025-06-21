/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.monobogdan.engine.internals;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author mono
 */
public class ObjMesh {
    public static class Vertex {
        public float X, Y, Z;

        public Vertex(float x, float y, float z) {
            X = x;
            Y = y;
            Z = z;
        }
    }

    public static class Normal {
        public float X, Y, Z;

        public Normal(float x, float y, float z) {
            X = x;
            Y = y;
            Z = z;
        }
    }

    public static class UV {
        public float U, V;

        public UV(float u, float v) {
            U = u;
            V = v;
        }
    }

    public static class Face {
        public String MaterialName;
        public int[] Vertex, Normal, UV;

        public Face() {
            Vertex = new int[3];
            Normal = new int[3];
            UV = new int[3];
        }
    }

    public static class SubObject {
        public String Name;
        public ArrayList<Face> Faces;

        public SubObject() {
            Faces = new ArrayList<Face>();
        }
    }

    public ArrayList<Vertex> Vertices;
    public ArrayList<Normal> Normals;
    public ArrayList<UV> TexCoords;
    public ArrayList<SubObject> Objects;

    public ObjMesh(InputStream strm) throws IOException {
        if(strm != null) {
            BufferedReader bufStrm = new BufferedReader(new InputStreamReader(strm));
            String line = null;

            Vertices = new ArrayList<Vertex>();
            Normals = new ArrayList<Normal>();
            TexCoords = new ArrayList<UV>();
            Objects = new ArrayList<SubObject>();

            // Parser state
            SubObject currSubObject = new SubObject();
            currSubObject.Name = "root";
            String currMaterial = null;

            while((line = bufStrm.readLine()) != null) {
                line = line.trim();
                
                if(line.length() < 1 || line.charAt(0) == '#')
                    continue; // Comment, skip

                String[] split = line.split(" ");
                if(split[0].equals("o")) {
                    if(split.length < 2)
                        throw new IllegalStateException("Object field doesn't contain valid object name");

                    if(currSubObject.Faces.size() > 0) // Non-empty object
                        Objects.add(currSubObject);

                    currSubObject = new SubObject();
                    currSubObject.Name = split[1];
                }

                if(split[0].equals("v")) {
                    if(split.length < 4)
                        throw new IllegalStateException("Incorrect vertex format");

                    Vertex vert = new Vertex(Float.valueOf(split[1]), Float.valueOf(split[2]), Float.valueOf(split[3]));
                    Vertices.add(vert);
                }

                if(split[0].equals("vt")) {
                    if(split.length < 3)
                        throw new IllegalStateException("Incorrect uv format");

                    UV vert = new UV(Float.valueOf(split[1]), Float.valueOf(split[2]));
                    TexCoords.add(vert);
                }

                if(split[0].equals("vn")) {
                    if(split.length < 4)
                        throw new IllegalStateException("Incorrect normal format");

                    Normal vert = new Normal(Float.valueOf(split[1]), Float.valueOf(split[2]), Float.valueOf(split[3]));
                    Normals.add(vert);
                }

                if(split[0].equals("usemtl")) {
                    if(split.length < 2)
                        throw new IllegalStateException("usemtl was empty");

                    currMaterial = split[1];
                }

                if(split[0].equals("f")) {
                    if(split.length > 4)
                        throw new IllegalStateException("Only triangulated meshes are supported");

                    Face face = new Face();

                    for(int i = 0; i < 3; i++) {
                        String[] desc = split[i + 1].split("/");
                        // Needs testing here, since input obj's might be in multiple formats. Assume v/n/uv.

                        face.MaterialName = currMaterial;
                        face.Vertex[i] = Integer.valueOf(desc[0]) - 1;
                        face.Normal[i] = Integer.valueOf(desc[2]) - 1;
                        face.UV[i] = Integer.valueOf(desc[1]) - 1;
                    }

                    currSubObject.Faces.add(face);
                }
            }

            Objects.add(currSubObject);
        }
    }
}
