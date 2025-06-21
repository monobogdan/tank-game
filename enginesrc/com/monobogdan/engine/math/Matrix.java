package com.monobogdan.engine.math;

public final class Matrix {
    public float Matrix[] = new float[16];

    public static final float DEG_TO_RAD = 0.0174533f;

    private static float[] identity = new float[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    };

    private float[] tmpMatrix = new float[16]; // Reduce allocations
    private float[] productMatrix = new float[16];

    public static Matrix Identity = new Matrix();

    public Matrix() {
        identity();
    }

    public Matrix(float[] values) {
        Matrix = values.clone();
    }

    public void identity() {
        Matrix = identity.clone();
    }

    public void multiply(Matrix matrix) {
        multiply(matrix.Matrix);
    }

    public static float getItem(float[] matrix, int x, int y) {
        return matrix[y * 4 + x];
    }

    // TODO: Inline matrix multiplication to make code faster
    public void multiply(float[] matrix) {
        tmpMatrix = Matrix.clone();

        for (int j = 0; j < 4; j++) {
            for (int i = 0; i < 4; i++) {
                Matrix[i * 4 + j] = getItem(tmpMatrix, j, 0) * getItem(matrix, 0, i) +
                        getItem(tmpMatrix, j, 1) * getItem(matrix, 1, i) +
                        getItem(tmpMatrix, j, 2) * getItem(matrix, 2, i) +
                        getItem(tmpMatrix, j, 3) * getItem(matrix, 3, i);
            }
        }
    }

    private void productIdentity() {
        System.arraycopy(identity, 0, productMatrix, 0, 16);
    }

    public void shadow(Vector light, Plane plane) {
        productIdentity();

        plane.normalize();
        float d = light.dotPlane(plane);

        productMatrix[0] = plane.A * light.X + d;
        productMatrix[1] = plane.A * light.Y;
        productMatrix[2] = plane.A * light.Z;
        productMatrix[3] = 0; // For directional light

        productMatrix[4] = plane.B * light.X;
        productMatrix[5] = plane.B * light.Y + d;
        productMatrix[6] = plane.B * light.Z;
        productMatrix[7] = 0;

        productMatrix[8] = plane.C * light.X;
        productMatrix[9] = plane.C * light.Y;
        productMatrix[10] = plane.C * light.Z + d;
        productMatrix[11] = 0;

        productMatrix[12] = plane.D * light.X;
        productMatrix[13] = plane.D * light.Y;
        productMatrix[14] = plane.D * light.Z;
        productMatrix[15] = plane.D * 0 + d;

        multiply(productMatrix);

        /*
        P.a * L.x + d  P.a * L.y      P.a * L.z      P.a * L.w
        P.b * L.x      P.b * L.y + d  P.b * L.z      P.b * L.w
        P.c * L.x      P.c * L.y      P.c * L.z + d  P.c * L.w
        P.d * L.x      P.d * L.y      P.d * L.z      P.d * L.w + d

        From MSDN
         */
    }

    public void translate(float x, float y, float z) {
        productIdentity();
        productMatrix[3 * 4] = x;
        productMatrix[3 * 4 + 1] = y;
        productMatrix[3 * 4 + 2] = z;

        multiply(productMatrix);
    }

    public void rotateAngleAxis(float angle, float x, float y, float z) {
        productIdentity();


    }

    public void rotateX(float angle) {
        float sin = (float)Math.sin(angle * DEG_TO_RAD);
        float cos = (float)Math.cos(angle * DEG_TO_RAD);

        productIdentity();
        productMatrix[5] = cos;
        productMatrix[6] = sin;
        productMatrix[9] = -sin;
        productMatrix[10] = cos;

        multiply(productMatrix);
    }

    public void rotateY(float angle) {
        float sin = (float)Math.sin(angle * DEG_TO_RAD);
        float cos = (float)Math.cos(angle * DEG_TO_RAD);

        productIdentity();
        productMatrix[0] = cos;
        productMatrix[2] = -sin;
        productMatrix[8] = sin;
        productMatrix[10] = cos;

        multiply(productMatrix);
    }

    public void rotateZ(float angle) {
        float sin = (float)Math.sin(angle * DEG_TO_RAD);
        float cos = (float)Math.cos(angle * DEG_TO_RAD);

        productIdentity();
        productMatrix[0] = cos;
        productMatrix[1] = sin;
        productMatrix[4] = -sin;
        productMatrix[5] = cos;

        multiply(productMatrix);
    }

    public void scale(float x, float y, float z) {
        productIdentity();
        productMatrix[0] = x;
        productMatrix[5] = y;
        productMatrix[10] = z;

        multiply(productMatrix);
    }

    public void set(int x, int y, float val) {
        Matrix[y * 4 + x] = val;
    }

    // This method works as well as perspective
    public void ortho(float width, float height, float near, float far) {
        System.arraycopy(identity, 0, Matrix, 0, 16);

        Matrix[0] = 2 / width;
        Matrix[5] = 2 / height;
        Matrix[10] = 1 / (far - near);
        Matrix[14] = near / (near - far);
        Matrix[15] = 1;

        /*
        2/w  0    0           0
        0    2/h  0           0
        0    0    1/(zf-zn)   0
        0    0    zn/(zn-zf)  1*/
    }

    // This method ASSUMES that this matrix will only be used as projection.
    // It saves some CPU cycles by getting rid of matrix multiplication
    public void perspective(float fov, float aspect, float near, float far) {
        // TODO: Clone makes allocations. Review code later for performance to make copy, not realloc
        Matrix = identity.clone();

        float yScale = 1.0f / (float)Math.tan((fov * DEG_TO_RAD) / 2);
        float xScale = yScale / aspect;

        /*set(0, 0, xScale);
        set(1, 1, yScale);
        set(2, 2, far / (far - near));
        set(3, 2, 1);
        set(2, 3, -near * far / (far - near));;
        set(3, 3, 0);*/

        Matrix[0] = xScale;
        Matrix[5] = yScale;
        Matrix[10] = far / (far - near);
        Matrix[11] = 1;
        Matrix[14] = -near * far / (far - near);
        Matrix[15] = 0;
    }

    public Vector transformVector(Vector vector) {
        return new Vector((vector.X * Matrix[0]) + (vector.Y * Matrix[4]) + (vector.Z * Matrix[8]) + (1 * Matrix[12]),
                (vector.X * Matrix[1]) + (vector.Y * Matrix[5]) + (vector.Z * Matrix[9]) + (1 * Matrix[13]),
                (vector.X * Matrix[2]) + (vector.Y * Matrix[6]) + (vector.Z * Matrix[10]) + (1 * Matrix[14]));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++)
                builder.append(Matrix[i * 4 + j]).append(' ');
            builder.append("    ");
        }

        return builder.toString();
    }
}
