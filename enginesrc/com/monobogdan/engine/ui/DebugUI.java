package com.monobogdan.engine.ui;

import com.monobogdan.engine.Runtime;
import com.monobogdan.engine.math.Color;
import com.monobogdan.engine.math.Vector;

public class DebugUI {
    private Runtime runtime;

    public BitmapFont Font;

    public DebugUI(Runtime runtime) {
        this.runtime = runtime;

        Font = BitmapFont.load(runtime, "font/default.font");
        if(Font == null)
            throw new RuntimeException("Can't load font for DebugUI");
    }

    private float drawString(float yOffset, String fmt, Object... params) {
        final int MARGIN = 8;
        runtime.Graphics.Canvas.drawString(Font, Color.White, 8, yOffset, String.format(fmt, params));

        return yOffset + Font.Size + MARGIN;
    }

    public void draw() {
        float yOffset = 0;

        yOffset = drawString(yOffset, "Triangles: %d", runtime.Graphics.FrameStatistics.TriangleCount);
        yOffset = drawString(yOffset, "Batches: %d", runtime.Graphics.FrameStatistics.DrawCalls);
        yOffset = drawString(yOffset, "Frames per second: %d", runtime.Time.FramesPerSecond);
    }
}
