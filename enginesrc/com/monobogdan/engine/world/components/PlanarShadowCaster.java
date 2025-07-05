package com.monobogdan.engine.world.components;

import com.monobogdan.engine.Camera;
import com.monobogdan.engine.Graphics;
import com.monobogdan.engine.Material;
import com.monobogdan.engine.Mesh;
import com.monobogdan.engine.math.Matrix;
import com.monobogdan.engine.math.Plane;
import com.monobogdan.engine.math.Vector;


public class PlanarShadowCaster implements ShadowCaster {
    private Matrix shadowMatrix = new Matrix();
    private Plane plane = new Plane(Vector.Down, 0.95f);


    public PlanarShadowCaster() {
        //material.DepthWrite = false;

    }

    public void rebuildMatrix(Matrix baseMatrix, Vector dirLight) {
        shadowMatrix = new Matrix(baseMatrix.Matrix);
        shadowMatrix.shadow(dirLight, plane);
    }

    @Override
    public void onDrawShadow(Mesh mesh, Graphics graphics, Camera camera, int renderPassFlags) {

    }
}
