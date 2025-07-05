package com.monobogdan.engine.math;

public class Plane {
    public float A, B, C, D;

    public Plane() {

    }

    public Plane(Vector normal, float distance) {
        Vector v = new Vector();
        v.set(normal);
        v.normalize();

        A = v.X;
        B = v.Y;
        C = v.Z;
        D = distance;
    }

    public Plane set(float a, float b, float c, float dist) {
        A = a;
        B = b;
        C = c;
        D = dist;

        return this;
    }

    public Plane normalize() {
        float magnitude = (float)Math.sqrt((A * A) + (B * B) + (C * C) + (D * D));

        A /= magnitude;
        B /= magnitude;
        C /= magnitude;
        D /= magnitude;

        return this;
    }
}
