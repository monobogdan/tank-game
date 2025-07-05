package com.monobogdan.engine;

import com.monobogdan.engine.desktop.Context;
import com.monobogdan.engine.math.Matrix;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.ui.Canvas;
import com.monobogdan.engine.world.components.ParticleSystem;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.opengl.GLContext;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;

public class Graphics extends BaseGraphics {
    private static final int LIGHT_COUNT = 8;
    private static final int IMMEDIATE_VERTEX_BUFFER_SIZE = 4096;

    public static final int BUFFER_PATH_VERTEX_POINTERS = 0;
    public static final int BUFFER_PATH_VBO = 1;

    private Context context;
    public Canvas Canvas;

    int BufferPath;

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

    private void chooseRenderPath() {
        String version = glGetString(GL_VERSION).trim();
        GPUClass = com.monobogdan.engine.GPUClass.detect(context, glGetString(GL_RENDERER), version);

        if(!GLContext.getCapabilities().OpenGL15) {
            BufferPath = BUFFER_PATH_VERTEX_POINTERS;
        } else {
            BufferPath = BUFFER_PATH_VBO;
        }
    }

    public Graphics(Context context) {
        this.context = context;

        context.log("Context version: %s", glGetString(GL_VERSION));
        context.log("Graphics card: %s", glGetString(GL_RENDERER));

        chooseRenderPath();

        context.log("Checking extension support");
        String extensions = glGetString(GL_EXTENSIONS);

        requireExtension(extensions, "GL_SGIS_generate_mipmap");

        orthoMatrix = new Matrix();

        // Initialize basic state
        glEnableClientState(GL_VERTEX_ARRAY);
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
        glLoadMatrix(matrixBuf);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        matrixBuf.put(camera.View.Matrix);
        matrixBuf.rewind();
        glLoadMatrix(matrixBuf);
        if(model) {
            matrixBuf.put(matrix.Matrix);
            matrixBuf.rewind();
            glMultMatrix(matrixBuf);
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

        for(int i = 0; i < Material.COMBINER_STAGE_COUNT; i++) {
            // Reset combiner state
            glActiveTexture(GL_TEXTURE0 + i);
            glDisable(GL_TEXTURE_2D);
        }

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
            setState(GL_TEXTURE_2D, material.Textures[0] != null);
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
                glMaterial(GL_FRONT_AND_BACK, GL_DIFFUSE, vectorBuf);

                vectorBuf.put(lightSources[i].Position.X);
                vectorBuf.put(lightSources[i].Position.Y);
                vectorBuf.put(lightSources[i].Position.Z);
                vectorBuf.put(lightSources[i].IsDirectional ? 0 : 1);
                vectorBuf.rewind();
                glLight(light, GL_POSITION, vectorBuf);
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
        glLight(lightNum, GL_AMBIENT, vectorBuf);

        vectorBuf.put(light.Specular.X);
        vectorBuf.put(light.Specular.Y);
        vectorBuf.put(light.Specular.Z);
        vectorBuf.put(0);
        vectorBuf.rewind();
        glLight(lightNum, GL_SPECULAR, vectorBuf);
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

        mesh.bind(part.VertexBufferOffset);
        int indexOffset = part.Offset * 2;
        if(BufferPath == BUFFER_PATH_VBO) {
            glDrawElements(GL_TRIANGLES, part.Count, GL_UNSIGNED_SHORT, indexOffset);
        } else {
            mesh.IndexData.position(indexOffset);
            glDrawElements(GL_TRIANGLES, part.Count, GL_UNSIGNED_SHORT, mesh.IndexData);
            mesh.IndexData.rewind();
        }

        FrameStatistics.TriangleCount += part.Count / 3;
        FrameStatistics.DrawCalls++;
    }

    public void drawMesh(Mesh mesh, Material material, Matrix matrix, Camera camera) {
        if(mesh != null) {
            for(BaseMesh.TriangleList list : mesh.TriangleLists.values())
                drawMeshPart(mesh, material, list, matrix, camera);
        }
    }

    private void prepareForImmediateMode() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void draw2DVertices(Texture2D tex, int topology, ArrayList<BaseMesh.UIVertex> vertices, int len) {
        if(vertices == null)
            throw new NullPointerException("VertexBuffer can't be null");

        if(len < 1)
            return;

        if(len > imVertexBuffer.capacity() / BaseMesh.UIVertex.Size) {
             context.log("Reallocating immediate vertex buffer. Old size %d new size %d", imVertexBuffer.capacity() / BaseMesh.UIVertex.Size, len);

            imVertexBuffer = ByteBuffer.allocateDirect(len * BaseMesh.UIVertex.Size).order(ByteOrder.nativeOrder());
        }

        // Prepare GL state
        if(BufferPath == BUFFER_PATH_VBO) {
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }

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
        glLoadMatrix(matrixBuf);

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

    public void drawBoundingBox(Camera camera, Vector min, Vector max, float x, float y, float z) {
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_LIGHTING);
        glDisable(GL_CULL_FACE);
        glDisable(GL_BLEND);

        glMatrixMode(GL_MODELVIEW);
        matrixBuf.put(camera.View.Matrix);
        matrixBuf.rewind();
        glLoadMatrix(matrixBuf);

        glBegin(GL_LINES);
        glColor3f(1, 0, 0);
        glVertex3f(x + min.X, 0, z + min.Z);
        glVertex3f(x + max.X, 0, z + min.Z);

        glVertex3f(x + min.X, 0, z + min.Z);
        glVertex3f(x + min.X, 0, z + max.Z);
        glEnd();
    }

    @Override
    public void drawParticles(Camera camera, Matrix matrix, java.util.Vector<ParticleSystem.Particle> particles) {
        prepareForImmediateMode();
        //setProjection(matrix, camera);

        glEnableClientState(GL_COLOR_ARRAY);

        for(int i = 0; i < particles.size(); i++) {
            ParticleSystem.Particle part = particles.get(i);

            // Create particle quad, facing to viewer

        }

        glDisableClientState(GL_COLOR_ARRAY);
    }

    public void drawLines(Line... lines) {

    }
}
