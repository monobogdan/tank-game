package com.monobogdan.engine.ui;

import com.monobogdan.engine.*;
import com.monobogdan.engine.math.Color;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.Graphics;
import com.monobogdan.engine.Texture2D;

import java.util.ArrayList;

/**
 * Created by mono on 06.06.2025.
 */

public class Canvas {
    private class BatchState {
        private static final int BATCH_RECTANGLE_COUNT = 64 * 6; // Approx rectangles per batch

        public Texture2D BoundTexture;
        public ArrayList<BaseMesh.UIVertex> Vertices = new ArrayList<BaseMesh.UIVertex>(BATCH_RECTANGLE_COUNT);
        public int UsedVertices;

        public void reset() {
            BoundTexture = null;
            UsedVertices = 0;
        }

        public void pushVertex(float x, float y, float z, float u, float v, Color color) {
            Vertices.ensureCapacity(UsedVertices + 1);

            BaseMesh.UIVertex vert = null;

            if(UsedVertices < Vertices.size())
                vert = Vertices.get(UsedVertices);
            else
                Vertices.add(vert = new BaseMesh.UIVertex(0, 0, 0, 0, 0));

            vert.setPosition(x, y, z).setColor(color.R, color.G, color.B, color.A).setUV(u, v);
            UsedVertices++;
        }
    }

    private Graphics graphics;

    private BatchState batchState;

    public Canvas(Graphics graphics) {
        this.graphics = graphics;

        batchState = new BatchState();
    }

    public boolean visibilityTest(float x, float y, float width, float height) {
        return x >= 0 && y >= 0 && x + width <= graphics.Viewport.Width;
    }

    public void drawString(BitmapFont font, Color color, float x, float y, String str) {
        if(font == null)
            throw new NullPointerException("font was null");

        if(str == null)
            return;

        if(color == null)
            color = Color.White;

        int sz = font.Size / 2;

        beginBatch(font.Pages[0]);

        for(int i = 0; i < str.length(); i++) {
            char chr = str.charAt(i);

            if(chr == ' ')
                x += sz;
            else {
                BitmapFont.CharacterInfo chrInfo = font.getCharacter(chr);

                if(chrInfo.Page > 0)
                    throw new UnsupportedOperationException("Multi-page batching for fonts not implemented yet");

                drawImage(x, y + chrInfo.YOffset, chrInfo.X, chrInfo.Y, chrInfo.Width, chrInfo.Height, chrInfo.Width, chrInfo.Height, color);
                x += chrInfo.Width;
            }
        }

        endBatch();
    }

    public void beginBatch(Texture2D tex) {
        if(tex == null)
            throw new RuntimeException("Expected texture for batch");

        batchState.BoundTexture = tex;
    }

    public void endBatch() {
        if(batchState.BoundTexture == null)
            throw new RuntimeException("Expected beginBatch before endBatch");

        graphics.draw2DVertices(batchState.BoundTexture, Graphics.TOPOLOGY_TRIANGLES, batchState.Vertices, batchState.UsedVertices);
        batchState.reset();
    }

    public void drawImage(float x, float y, float srcX, float srcY, float srcWidth, float srcHeight, float width, float height, Color color) {
        if(batchState.BoundTexture == null)
            throw new RuntimeException("Expected beginBatch before drawImage");

        if(visibilityTest(x, y, width, height)) {
            Texture2D tex = batchState.BoundTexture;

            x -= graphics.Viewport.Width / 2;
            y -= graphics.Viewport.Height / 2;

            srcX /= tex.Width;
            srcY /= tex.Height;
            float widthU = srcWidth / tex.Width;
            float heightV = srcHeight / tex.Height;

            batchState.pushVertex(x + width, y + height, 0, srcX + widthU, srcY + heightV, color);
            batchState.pushVertex(x, y + height, 0, srcX, srcY + heightV, color);
            batchState.pushVertex(x, y, 0, srcX, srcY, color);

            batchState.pushVertex(x, y, 0,  srcX, srcY, color);
            batchState.pushVertex(x + width, y, 0, srcX + widthU, srcY, color);
            batchState.pushVertex(x + width, y + height, 0, srcX + widthU, srcY + heightV, color);

            /*rectVerts[2].setPosition(x, y, 0).setUV(srcX, srcY);
            rectVerts[1].setPosition(x, y + height, 0).setUV(srcX, srcY + heightV);
            rectVerts[0].setPosition(x + width, y + height, 0).setUV(srcX + widthU, srcY + heightV);
            rectVerts[3].setPosition(x, y, 0).setUV(srcX, srcY);
            rectVerts[4].setPosition(x + width, y, 0).setUV(srcX + widthU, srcY);
            rectVerts[5].setPosition(x + width, y + height, 0).setUV(srcX + widthU, srcY + heightV);*/


        }
    }

    // Convenience method, same to Graphics.drawLines
    public void drawLines(Line... lines) {

    }
}
