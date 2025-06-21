package com.monobogdan.engine;

import com.monobogdan.engine.desktop.Context;
import com.monobogdan.engine.math.Matrix;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.ui.Canvas;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;

public class Graphics extends BaseGraphics {
    private static final int LIGHT_COUNT = 8;
    private static final int IMMEDIATE_VERTEX_BUFFER_SIZE = 4096;

    private Context context;
    public Canvas Canvas;

    private FloatBuffer imVertexBuffer = ByteBuffer.allocateDirect(12 * IMMEDIATE_VERTEX_BUFFER_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer(),
            imUVBuffer = ByteBuffer.allocateDirect(8 * IMMEDIATE_VERTEX_BUFFER_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer(),
            imColorBuffer = ByteBuffer.allocateDirect(16 * IMMEDIATE_VERTEX_BUFFER_SIZE).order(ByteOrder.nativeOrder()).asFloatBuffer();
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

    public Graphics(Context context) {
        this.context = context;

        context.log("Context version: %s", glGetString(GL_VERSION));
        context.log("Graphics card: %s", glGetString(GL_RENDERER));

        context.log("Checking extension support");
        String extensions = glGetString(GL_EXTENSIONS);

        requireExtension(extensions, "GL_SGIS_generate_mipmap");

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

    private void setProjection(Matrix matrix, Camera camera) {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        matrixBuf.put(camera.Projection.Matrix);
        matrixBuf.rewind();
        glLoadMatrix(matrixBuf);

        glMatrixMode(GL_MODELVIEW);
        matrixBuf.put(camera.View.Matrix);
        matrixBuf.rewind();
        glLoadMatrix(matrixBuf);
        matrixBuf.put(matrix.Matrix);
        matrixBuf.rewind();
        glMultMatrix(matrixBuf);
    }

    private void setMaterial(Material material) {
        if(currentMaterial != null && currentMaterial.equals(material))
            return;

        setState(GL_DEPTH_TEST, material.DepthTest);
        glDepthMask(material.DepthWrite);
        setState(GL_ALPHA_TEST, material.AlphaTest);
        setState(GL_BLEND, material.AlphaBlend);
        setState(GL_TEXTURE_2D, material.Diffuse != null);
        setState(GL_LIGHTING, true);

        if(material.AlphaTest)
            glAlphaFunc(GL_LESS, material.AlphaTestValue);

        if(material.Diffuse != null) {
            glClientActiveTexture(GL_TEXTURE0);
            material.Diffuse.bind();
        } else {
            glBindTexture(GL_TEXTURE_2D, 0);
        }

        if(material.Detail != null) {
            glClientActiveTexture(GL_TEXTURE1);
            material.Detail.bind();
        }

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

    void drawMeshPart(Mesh mesh, Material material, BaseMesh.TriangleList part, Matrix matrix, Camera camera) {
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

        setProjection(matrix, camera);
        setMaterial(material);

        glEnableClientState(GL_NORMAL_ARRAY);

        mesh.bind();
        glVertexPointer(3, GL_FLOAT, BaseMesh.Vertex.Size, BaseMesh.Vertex.Size * part.VertexBufferOffset);
        glTexCoordPointer(2, GL_FLOAT, BaseMesh.Vertex.Size, (BaseMesh.Vertex.Size * part.VertexBufferOffset) + 24);
        glNormalPointer(GL_FLOAT, BaseMesh.Vertex.Size, (BaseMesh.Vertex.Size * part.VertexBufferOffset) + 12);
        glDrawElements(GL_TRIANGLES, part.Count, GL_UNSIGNED_SHORT, part.Offset * 2);
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

    public void drawVertexBufferOrtho(Texture2D tex, int topology, BaseMesh.Vertex[] vertices, int len, Vector color) {
        if(vertices == null)
            throw new NullPointerException("VertexBuffer can't be null");

        if(vertices.length >= imVertexBuffer.capacity() / 12)
            throw new RuntimeException("Attempt to draw " + vertices.length + " vertices, but immediateVertexBuffer has capacity only for " + (imVertexBuffer.capacity() / 12) + " vertices");

        // Prepare GL state
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

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

        for(BaseMesh.Vertex vert : vertices) {
            imVertexBuffer.put(vert.X);
            imVertexBuffer.put(vert.Y);
            imVertexBuffer.put(vert.Z);

            imUVBuffer.put(vert.U);
            imUVBuffer.put(vert.V);

            imColorBuffer.put(color.X);
            imColorBuffer.put(color.Y);
            imColorBuffer.put(color.Z);
        }
        imVertexBuffer.rewind();
        imUVBuffer.rewind();
        imColorBuffer.rewind();
        glVertexPointer(3, 12, imVertexBuffer);
        glTexCoordPointer(2, 8, imUVBuffer);
        glColorPointer(3, 12, imColorBuffer);
        glDrawArrays(GL_TRIANGLES, 0, len);

        glDisableClientState(GL_COLOR_ARRAY);
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

    // For 2D operations
    public void drawVertexBufferOrtho(Texture2D tex, int topology, BaseMesh.Vertex... vertices) {
        drawVertexBufferOrtho(tex, topology, vertices, vertices.length, Vector.One);
    }

    public void drawLines(Line... lines) {

    }
}
