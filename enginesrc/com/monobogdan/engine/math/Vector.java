package com.monobogdan.engine.math;

public class Vector {
    private static float EPSILON = 0.00001F;

    public static Vector Zero = new Vector(0, 0, 0);
    public static Vector One = new Vector(1, 1, 1);
    public static Vector Up = new Vector(0, 1, 0);
    public static Vector Down = new Vector(0, -1, 0);
    public static Vector Left = new Vector(1, 0, 0);
    public static Vector Forward = new Vector(0, 0, 1);

    public float X, Y, Z;

    public Vector(float x, float y, float z) {
        X = x;
        Y = y;
        Z = z;
    }

    public Vector() { }

    public void add(Vector b) {
        X += b.X;
        Y += b.Y;
        Z += b.Z;
    }

    public void subtract(Vector b) {
        X = X - b.X;
        Y = Y - b.Y;
        Z = Z - b.Z;
    }

    public void multiply(float val) {
        X = X * val;
        Y = Y * val;
        Z = Z * val;
    }

    public boolean compare(Vector v, float epsilon) {
        return MathUtils.abs(X - v.X) < epsilon && MathUtils.abs(Y - v.Y) < epsilon && MathUtils.abs(Z - v.Z) < epsilon;
    }

    public Vector multiply(Matrix matrix) {
        return null; // Not implemented yet
    }

    public void lerp(Vector start, Vector end, float position) {
        X = MathUtils.lerp(start.X, end.X, position);
        Y = MathUtils.lerp(start.Y, end.Y, position);
        Z = MathUtils.lerp(start.Z, end.Z, position);
    }

    public float magnitude() {
        return (X * X) + (Y * Y) + (Z * Z);
    }

    public float dot(Vector b) {
        return (X * b.X) + (Y * b.Y) + (Z * b.Z);
    }

    public float dotPlane(Plane b) {
        return (X * b.A) + (Y * b.B) + (Z * b.C); // W * Plane.W = 0, so omit it
    }

    public void clamp(float min, float max) {
        X = MathUtils.clamp(X, min, max);
        Y = MathUtils.clamp(Y, min, max);
        Z = MathUtils.clamp(Z, min, max);
    }

    public void calculateForward(Vector rotation) {
        X = -(float)Math.sin(rotation.Y * Matrix.DEG_TO_RAD);
        Y = (float)Math.sin(rotation.X * Matrix.DEG_TO_RAD);
        Z = -(float)Math.cos(rotation.Y * Matrix.DEG_TO_RAD);
    }

    public Vector cross(Vector b) {
        return new Vector(Y * b.Z - Z * b.Y,
                Z * b.X - X * b.Z,
                X * Y - Y * b.X);
    }

    public void set(float x, float y, float z) {
        X = x;
        Y = y;
        Z = z;
    }

    public void set(Vector vec) {
        X = vec.X;
        Y = vec.Y;
        Z = vec.Z;
    }

    public void normalize() {
        float magnitude = magnitude();

        if(magnitude > EPSILON) {
            X /= magnitude;
            Y /= magnitude;
            Z /= magnitude;
        } else {
            X = 0;
            Y = 0;
            Z = 0;
        }
    }

    public static Vector fromColor(int r, int g, int b) {
        return new Vector((float)r / 255, (float)g  / 255, (float)b / 255);
    }

    @Override
    public String toString() {
        return String.format("%f %f %f", X, Y, Z);
    }
}
