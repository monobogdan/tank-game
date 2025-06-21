package com.monobogdan.engine.math;

public class Plane {
    public float A, B, C, D;

    public Plane(Vector normal, float distance) {
        Vector v = new Vector();
        v.set(normal);
        v.normalize();

        A = v.X;
        B = v.Y;
        C = v.Z;
        D = distance;
    }

    public void normalize() {
        float magnitude = (A * A) + (B * B) + (C * C) + (D * D);

        A /= magnitude;
        B /= magnitude;
        C /= magnitude;
        D /= magnitude;
    }
}
