package com.monobogdan.engine;

import com.monobogdan.engine.math.Matrix;
import com.monobogdan.engine.math.Vector;

public class Camera {
    public Vector Position;
    public Vector Rotation;

    public Matrix View;
    public Matrix Projection;

    public Vector Forward;
    //public Vector Right;
    //public Vector Up;

    public boolean UseRawMatrices;

    public Camera() {
        View = new Matrix();
        Projection = new Matrix();

        Projection.perspective(75.0f, (float)800.0f / 600, 0.1f, 1000);
        View.translate(-0, 0, 4);

        Position = new Vector(0, 0, 0);
        Rotation = new Vector(0, 0, 0);

        Forward = new Vector(0, 0, 0);
    }

    public void calculateVectors() {
        if(!UseRawMatrices) {
            Forward.X = (float)Math.sin(Rotation.Y * Matrix.DEG_TO_RAD);
            Forward.Y = (float)Math.sin(Rotation.X * Matrix.DEG_TO_RAD);
            Forward.Z = (float)Math.cos(Rotation.Y * Matrix.DEG_TO_RAD);
        }


    }

    public void updateProjection() {
        if(!UseRawMatrices) {
            View.identity();
            View.rotateZ(-Rotation.Z);
            View.rotateX(-Rotation.X);
            View.rotateY(-Rotation.Y);
            View.translate(-Position.X, -Position.Y, -Position.Z);
        }
    }
}
