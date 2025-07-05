package com.monobogdan.engine;

import android.content.Context;
import android.util.Log;

import com.monobogdan.engine.BaseGraphics;
import com.monobogdan.engine.BaseMesh;
import com.monobogdan.engine.Camera;
import com.monobogdan.engine.Line;
import com.monobogdan.engine.android.MainActivity;
import com.monobogdan.engine.math.Matrix;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.ui.Canvas;
import com.monobogdan.engine.world.components.ParticleSystem;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import static android.opengl.GLES11.*;
import static android.opengl.GLES11Ext.*;

/**
 * Created by mono on 10.06.2025.
 */

public class Graphics extends BaseGraphics {


    private static final int LIGHT_COUNT = 8;

    private MainActivity context;
    public com.monobogdan.engine.ui.Canvas Canvas;

    private ByteBuffer imVertexBuffer = ByteBuffer.allocateDirect(1).order(ByteOrder.nativeOrder());
    private Matrix orthoMatrix;
    private ByteBuffer matrixBuffer;
    private FloatBuffer matrixBuf;
    private FloatBuffer vectorBuf = ByteBuffer.allocateDirect(16).order(ByteOrder.nativeOrder()).asFloatBuffer();

    private Light[] lightSources = new Light[LIGHT_COUNT];

    private Material currentMaterial;

    private void requireExtension(String extList, String extName) {
        if(!extList.contains(extName))
            throw new RuntimeException("Missing required OpenGL extension " + extName);
    }

    public Graphics(MainActivity context) {
        this.context = context;

        GPUClass = com.monobogdan.engine.GPUClass.detect(context, glGetString(GL_RENDERER), glGetString(GL_VERSION));

        context.log("Context version: %s", glGetString(GL_VERSION));
        context.log("GPU: %s", glGetString(GL_RENDERER));

        context.log("Checking extension support");
        String extensions = glGetString(GL_EXTENSIONS);

        orthoMatrix = new Matrix();

        // Initialize basic state
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnableClientState(GL_NORMAL_ARRAY);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glEnable(GL_LIGHTING);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);

        matrixBuffer = ByteBuffer.allocateDirect(4 * 16);
        matrixBuffer.order(ByteOrder.nativeOrder());
        matrixBuf = matrixBuffer.asFloatBuffer();

        Canvas = new Canvas(this);
    }

    public void clear(float r, float g, float b) {
        glClearColor(r, g, b, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
    }

    public void setViewport(int width, int height) {
        Viewport.Width = width;
        Viewport.Height = height;
        Viewport.AspectRatio = (float)width / height;

        glViewport(0, 0, width, height);

        orthoMatrix.ortho(Viewport.Width, -Viewport.Height, 0, 1);
    }

    public void copyToRenderTarget(Texture2D rt) {
        if(rt == null)
            throw new NullPointerException("RenderTarget was null");

        rt.bind();
        glCopyTexSubImage2D(rt.ID, 0, 0, 0, 0, 0, rt.Width, rt.Height);
    }

    private void setState(int state, boolean on) {
        if(on)
            glEnable(state);
        else
            glDisable(state);
    }

    private void setProjection(Matrix matrix, Camera camera, boolean model) {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        matrixBuf.put(camera.Projection.Matrix);
        matrixBuf.rewind();
        glLoadMatrixf(matrixBuf);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        matrixBuf.put(camera.View.Matrix);
        matrixBuf.rewind();
        glLoadMatrixf(matrixBuf);
        if(model) {
            matrixBuf.put(matrix.Matrix);
            matrixBuf.rewind();
            glMultMatrixf(matrixBuf);
        }
    }

    private int setMaterial(Material material) {
        if(currentMaterial != null && currentMaterial.equals(material))
            return material.Shaders.length;

        setState(GL_DEPTH_TEST, material.DepthTest);
        glDepthMask(material.DepthWrite);
        setState(GL_ALPHA_TEST, material.AlphaTest);
        setState(GL_BLEND, material.AlphaBlend);
        setState(GL_LIGHTING, true);

        if(material.AlphaTest)
            glAlphaFunc(GL_LESS, material.AlphaTestValue);

        if(GPUClass.QualityLevel >= com.monobogdan.engine.GPUClass.QUALITY_LEVEL_NORMAL) {
            for (int i = 0; i < Material.COMBINER_STAGE_COUNT; i++) {
                // Reset combiner state
                glActiveTexture(GL_TEXTURE0 + i);
                glDisable(GL_TEXTURE_2D);
            }

            for (int i = 0; i < material.Shaders.length; i++) {
                Material.ShaderInstance instance = material.Shaders[i];

                glActiveTexture(GL_TEXTURE0 + i);
                glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE);
                glEnable(GL_TEXTURE_2D);
                instance.Shader.onApply(material, i, instance.Params);
            }
        } else {
            // Single texture fallback for very slow GPU's
            glActiveTexture(GL_TEXTURE0);
            setState(GL_TEXTURE_2D, true);
            material.Textures[0].bind();
        }

        /*setState(GL_TEXTURE_2D, material.Diffuse != null);

        if(material.Diffuse != null) {
            glClientActiveTexture(GL_TEXTURE0);
            material.Diffuse.bind();
        } else {
            glBindTexture(GL_TEXTURE_2D, 0);
        }

        if(material.Detail != null) {
            glClientActiveTexture(GL_TEXTURE1);
            material.Detail.bind();
        }*/

        for(int i = 0; i < LIGHT_COUNT; i++) {
            int light = GL_LIGHT0 + i;
            if(lightSources[i] == null) {
                setState(light, false);
            } else {
                setState(light, true);

                vectorBuf.put(material.R);
                vectorBuf.put(material.G);
                vectorBuf.put(material.B);
                vectorBuf.put(material.A);
                vectorBuf.rewind();
                glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, vectorBuf);

                vectorBuf.put(lightSources[i].Position.X);
                vectorBuf.put(lightSources[i].Position.Y);
                vectorBuf.put(lightSources[i].Position.Z);
                vectorBuf.put(lightSources[i].IsDirectional ? 0 : 1);
                vectorBuf.rewind();
                glLightfv(light, GL_POSITION, vectorBuf);
            }
        }

        currentMaterial = material;

        return material.Shaders.length;
    }

    // This method might be rewritten in future to support 8 lights per draw call, not per scene
    public void setLightSource(int num, Light light) {
        if(num < 0 || num >= LIGHT_COUNT)
            return; // Only 0..7 light sources are supported in FFP

        if(light == null)
            throw new NullPointerException("light was null");

        lightSources[num] = light;

        int lightNum = GL_LIGHT0 + num;

        vectorBuf.put(light.Ambient.X);
        vectorBuf.put(light.Ambient.Y);
        vectorBuf.put(light.Ambient.Z);
        vectorBuf.put(0);
        vectorBuf.rewind();
        glLightfv(lightNum, GL_AMBIENT, vectorBuf);

        vectorBuf.put(light.Specular.X);
        vectorBuf.put(light.Specular.Y);
        vectorBuf.put(light.Specular.Z);
        vectorBuf.put(0);
        vectorBuf.rewind();
        glLightfv(lightNum, GL_SPECULAR, vectorBuf);
    }

    public void drawMeshPart(Mesh mesh, Material material, BaseMesh.TriangleList part, Matrix matrix, Camera camera) {
        if(camera == null)
            throw new NullPointerException("No camera supplied");

        if(matrix == null)
            throw new NullPointerException("No matrix supplied");

        if(material == null)
            throw new NullPointerException("Expected material for rendering");

        if(mesh == null)
            return; // Not a fatal error

        if(part == null)
            return; // Not a fatal error too, but still should take care (probably add warning)

        setProjection(matrix, camera, false);
        int usedCombiners = setMaterial(material);
        setProjection(matrix, camera, true);

        glEnableClientState(GL_NORMAL_ARRAY);

        mesh.bind();

        for(int i = 0; i < usedCombiners; i++) {
            glClientActiveTexture(GL_TEXTURE0 + i);
            glEnableClientState(GL_TEXTURE_COORD_ARRAY);
            glTexCoordPointer(2, GL_FLOAT, BaseMesh.Vertex.Size, (BaseMesh.Vertex.Size * part.VertexBufferOffset) + 24);
        }

        glVertexPointer(3, GL_FLOAT, BaseMesh.Vertex.Size, BaseMesh.Vertex.Size * part.VertexBufferOffset);
        glNormalPointer(GL_FLOAT, BaseMesh.Vertex.Size, (BaseMesh.Vertex.Size * part.VertexBufferOffset) + 12);
        glDrawElements(GL_TRIANGLES, part.Count, GL_UNSIGNED_SHORT, part.Offset * 2);

        FrameStatistics.TriangleCount += part.Count / 3;
        FrameStatistics.DrawCalls++;
    }

    public void drawMesh(Mesh mesh, Material material, Matrix matrix, Camera camera) {
        if(mesh != null) {
            for(int i = 0; i < mesh.LinearList.size(); i++)
                drawMeshPart(mesh, material, mesh.LinearList.get(i), matrix, camera);
        }
    }

    private void invalidateState() {
        currentMaterial = null;
    }

    @Override
    public void draw2DVertices(Texture2D tex, int topology, ArrayList<BaseMesh.UIVertex> vertices, int len) {
        if(vertices == null)
            throw new NullPointerException("VertexBuffer can't be null");

        if(len > vertices.size())
            throw new IndexOutOfBoundsException("len > vertices.size");

        if(len > imVertexBuffer.capacity() / BaseMesh.UIVertex.Size) {
            context.log("Reallocating immediate vertex buffer. Old size %d new size %d", imVertexBuffer.capacity() / BaseMesh.UIVertex.Size, len);

            imVertexBuffer = ByteBuffer.allocateDirect(len * BaseMesh.UIVertex.Size).order(ByteOrder.nativeOrder());
        }

        invalidateState();

        // Prepare GL state
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);


        for(int i = 0; i < Material.COMBINER_STAGE_COUNT; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glClientActiveTexture(GL_TEXTURE0 + i);
            glDisable(GL_TEXTURE_2D);
            glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        }

        glActiveTexture(GL_TEXTURE0);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glMatrixMode(GL_PROJECTION);
        matrixBuf.put(orthoMatrix.Matrix);
        matrixBuf.rewind();
        glLoadMatrixf(matrixBuf);

        glDisableClientState(GL_NORMAL_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_LIGHTING);
        glEnable(GL_BLEND);

        setState(GL_TEXTURE_2D, tex != null);
        if(tex != null)
            tex.bind();

        int vSize = 20; // Position and UV

        imVertexBuffer.rewind();

        for(int i = 0; i < len; i++) {
            BaseMesh.UIVertex vert = vertices.get(i);

            imVertexBuffer.putFloat(vert.X);
            imVertexBuffer.putFloat(vert.Y);
            imVertexBuffer.putFloat(vert.Z);

            imVertexBuffer.putFloat(vert.U);
            imVertexBuffer.putFloat(vert.V);

            imVertexBuffer.putFloat(vert.R);
            imVertexBuffer.putFloat(vert.G);
            imVertexBuffer.putFloat(vert.B);
            imVertexBuffer.putFloat(vert.A);
        }
        imVertexBuffer.rewind();
        glVertexPointer(3, GL_FLOAT, BaseMesh.UIVertex.Size, imVertexBuffer);
        glClientActiveTexture(GL_TEXTURE0);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        imVertexBuffer.position(12);
        glTexCoordPointer(2, GL_FLOAT, BaseMesh.UIVertex.Size, imVertexBuffer);
        imVertexBuffer.position(20);
        glColorPointer(4, GL_FLOAT, BaseMesh.UIVertex.Size, imVertexBuffer);
        glDrawArrays(GL_TRIANGLES, 0, len);

        glDisableClientState(GL_COLOR_ARRAY);

        FrameStatistics.TriangleCount += vertices.size() / 3;
        FrameStatistics.DrawCalls++;
    }

    @Override
    public void drawBoundingBox(Camera camera, Vector min, Vector max, float x, float y, float z) {

    }

    public void drawLines(Line... lines) {

    }

    @Override
    public void drawParticles(Camera camera, Matrix matrix, java.util.Vector<ParticleSystem.Particle> particles) {

    }
}