package com.monobogdan.engine;

import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.ui.Canvas;

public class BaseGraphics {
    public static int TOPOLOGY_TRIANGLES = 0;
    public static int TOPOLOGY_LINES = 1;

    public class FrameData {
        public int TriangleCount;
        public int DrawCalls;
        public int RenderTime;
        public int MemoryConsumption;
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
    }

    public Viewport Viewport;
    private Line[] lines = new Line[1];

    public interface RenderPass {
        void onRender(Graphics graphics, String passName);
    }

    public BaseGraphics() {
        Viewport = new Viewport();

        for(int i = 0; i < lines.length; i++)
            lines[i] = new Line();
    }

    public void doPass(String name, RenderPass renderPass) {
        if(renderPass == null)
            throw new NullPointerException("RenderPass can't be null for pass " + name);

        renderPass.onRender((Graphics)this, name); // This might seem ugly, but it's OK. There is only instance of Graphics in engine.
    }
}
