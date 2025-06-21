package com.monobogdan.engine;

import com.monobogdan.engine.math.Vector;

public class Line {
    public Vector From = new Vector();
    public Vector To = new Vector();
    public float R, G, B;

    public Line(Vector from, Vector to, float r, float g, float b) {
        From.set(from);
        To.set(to);

        R = r;
        G = g;
        B = b;
    }

    public Line() {
        R = 1.0f;
        G = 1.0f;
        B = 1.0f;
    }
}
