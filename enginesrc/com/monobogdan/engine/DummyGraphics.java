package com.monobogdan.engine;

import com.monobogdan.engine.ui.Canvas;

/**
 * Created by mono on 06.06.2025.
 */

/*
    Reference class for graphics implementation. Not used in production builds, just for reference
 */
public abstract class DummyGraphics {
    public class FrameData {
        public int TriangleCount;
        public int DrawCalls;
        public int RenderTime;
        public int MemoryConsumption;
    }

    private Canvas canvas;

    protected DummyGraphics() {
        throw new UnsupportedOperationException("How did you get there?");
    }

    public void beginPass() {

    }

    public void endPass() {

    }

    public void clear(float r, float g, float b) {

    }

    public void drawMesh() {

    }

    public void drawLines(Line... lines) {

    }

    public Canvas getCanvas() {
        return null;
    }
}
