package com.monobogdan.engine;

import com.monobogdan.engine.math.Matrix;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.world.components.ParticleSystem;

import java.util.ArrayList;

public abstract class BaseGraphics {
    public static int TOPOLOGY_TRIANGLES = 0;
    public static int TOPOLOGY_LINES = 1;

    public interface FixedFunctionShader {
        void onApply(Material material, int combiner, float[] params);
    }

    public class FrameData {
        public int TriangleCount;
        public int DrawCalls;
        public int MemoryConsumption;
        public int Occluded;

        public void reset() {
            TriangleCount = 0;
            DrawCalls = 0;
            MemoryConsumption = 0;
            Occluded = 0;
        }
    }

    public static class Light {
        public boolean IsDirectional = true;
        public Vector Diffuse = new Vector(1, 1, 1);
        public Vector Ambient = new Vector(0.4f, 0.4f, 0.4f);
        public Vector Specular = new Vector(1, 1, 1);
        public float Intensity = 100.0f;
        public Vector Position = new Vector(); // Direction in case of IsDirectional = true
    }

    public class Viewport {
        public int Width;
        public int Height;
        public float AspectRatio;
    }

    public interface RenderPass {
        void onRender(Graphics graphics, String passName);
    }

    public Viewport Viewport;
    public FrameData FrameStatistics;
    public GPUClass GPUClass;

    protected boolean IsFixedFunction;

    public BaseGraphics() {
        Viewport = new Viewport();
        FrameStatistics = new FrameData();
    }

    public void doPass(String name, RenderPass renderPass) {
        if(renderPass == null)
            throw new NullPointerException("RenderPass can't be null for pass " + name);

        renderPass.onRender((Graphics)this, name); // This might seem ugly, but it's OK. There is only instance of Graphics in engine.
    }

    public abstract void clear(float r, float g, float b);
    public abstract void setViewport(int width, int height);
    public abstract void copyToRenderTarget(Texture2D rt);
    public abstract void setLightSource(int num, Light light);
    public abstract void drawMeshPart(Mesh mesh, Material material, BaseMesh.TriangleList part, Matrix matrix, Camera camera);

    public final boolean isFixedFunction() {
        return this.IsFixedFunction;
    }

    public void drawMesh(Mesh mesh, Material material, Matrix matrix, Camera camera) {
        if(mesh != null) {
            for(BaseMesh.TriangleList list : mesh.TriangleLists.values())
                drawMeshPart(mesh, material, list, matrix, camera);
        }
    }

    public abstract void draw2DVertices(Texture2D tex, int topology, ArrayList<BaseMesh.UIVertex> vertices, int len);
    public abstract void drawBoundingBox(Camera camera, Vector min, Vector max, float x, float y, float z);

    // For 2D operations

    public abstract void drawLines(Line... lines);
    public abstract void drawParticles(Camera camera, Matrix matrix, java.util.Vector<ParticleSystem.Particle> particles);
}
