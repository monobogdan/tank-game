package com.monobogdan.engine.ui;

import com.monobogdan.engine.Input;
import com.monobogdan.engine.Runtime;
import com.monobogdan.engine.Texture2D;
import com.monobogdan.engine.math.Color;
import com.monobogdan.engine.math.Vector;

/**
 * Simple immediate mode UI implementation
 */
public class UI {
    private Runtime runtime;

    private Color tinted = new Color(0.8f, 0.8f, 0.8f, 1.0f);

    public UI(Runtime runtime) {
        this.runtime = runtime;
    }

    public boolean isRectanglePressed(float x, float y, float w, float h, boolean repeat) {
        int desiredState = repeat ? Input.STATE_PRESSED : Input.STATE_RELEASED;

        for(int i = 0; i < 10; i++) {
            Input.Touch touch = runtime.Input.getTouchState(i);

            if(touch == null)
                break;

            if(touch.State == desiredState && touch.X > x && touch.Y > y && touch.X < x + w && touch.Y < y + h)
                return true;
        }

        return false;
    }

    public float relativeToAbsoluteX(float x) {
        return x * runtime.Graphics.Viewport.Width;
    }

    public float relativeToAbsoluteY(float y) {
        return y * runtime.Graphics.Viewport.Height;
    }

    public boolean imageButton(Texture2D tex, float x, float y, float w, float h, boolean repeat) {
        x = relativeToAbsoluteX(x) / runtime.Graphics.Viewport.AspectRatio;
        w = relativeToAbsoluteX(w) / runtime.Graphics.Viewport.AspectRatio;
        y = relativeToAbsoluteY(y);
        h = relativeToAbsoluteY(h);

        boolean state = isRectanglePressed(x, y, w, h, repeat);

        runtime.Graphics.Canvas.beginBatch(tex);
        runtime.Graphics.Canvas.drawImage(x, y, 0, 0, tex.Width, tex.Height, w, h, state ? tinted : Color.White);
        runtime.Graphics.Canvas.endBatch();

        return state;
    }
}
