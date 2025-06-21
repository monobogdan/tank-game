package com.monobogdan.engine.ui;

import com.monobogdan.engine.*;
import com.monobogdan.engine.math.Vector;

/**
 * Created by mono on 06.06.2025.
 */

public class Canvas {
    private Graphics graphics;

    private static BaseMesh.Vertex[] rectVerts = new BaseMesh.Vertex[8];

    public Canvas(Graphics graphics) {
        this.graphics = graphics;

        for(int i = 0; i < rectVerts.length; i++)
            rectVerts[i] = new BaseMesh.Vertex(0, 0, 0, 0, 0);
    }

    public boolean visibilityTest(float x, float y, float width, float height) {
        return x >= 0 && y >= 0 && x + width <= graphics.Viewport.Width && y + height <= graphics.Viewport.Height;
    }

    public void drawString(BitmapFont font, Vector color, float x, float y, float size, String str) {
        if(font == null)
            throw new NullPointerException("font was null");

        if(str == null)
            return;

        for(int i = 0; i < str.length(); i++) {
            char chr = str.charAt(i);
            BitmapFont.CharacterInfo chrInfo = font.getCharacter(chr);

            drawImage(font.Pages[chrInfo.Page], x, y + chrInfo.YOffset, chrInfo.X, chrInfo.Y, chrInfo.Width, chrInfo.Height, chrInfo.Width, chrInfo.Height, color);

            x += size;
        }
    }

    public void drawImage(Texture2D tex, float x, float y, float width, float height) {
        drawImage(tex, x, y, 0, 0, tex.Width, tex.Height, width, height, Vector.One);
    }

    public void drawImage(Texture2D tex, float x, float y, float srcX, float srcY, float srcWidth, float srcHeight, float width, float height, Vector color) {
        if(tex == null)
            throw new RuntimeException("texture can't be null");

        if(visibilityTest(x, y, width, height)) {
            x -= graphics.Viewport.Width / 2;
            y -= graphics.Viewport.Height / 2;

            srcX /= tex.Width;
            srcY /= tex.Height;
            float widthU = srcWidth / tex.Width;
            float heightV = srcHeight / tex.Height;

            rectVerts[0].setPosition(x, y, 0).setUV(srcX, srcY);
            rectVerts[1].setPosition(x, y + height, 0).setUV(srcX, srcY + heightV);
            rectVerts[2].setPosition(x + width, y + height, 0).setUV(srcX + widthU, srcY + heightV);
            rectVerts[3].setPosition(x, y, 0).setUV(srcX, srcY);
            rectVerts[4].setPosition(x + width, y, 0).setUV(srcX + widthU, srcY);
            rectVerts[5].setPosition(x + width, y + height, 0).setUV(srcX + widthU, srcY + heightV);

            graphics.drawVertexBufferOrtho(tex, Graphics.TOPOLOGY_TRIANGLES, rectVerts, 6, color);
        }
    }

    // Convenience method, same to Graphics.drawLines
    public void drawLines(Line... lines) {

    }
}
