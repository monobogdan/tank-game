package com.monobogdan.engine.world.components;

import com.monobogdan.engine.Camera;
import com.monobogdan.engine.Graphics;
import com.monobogdan.engine.Material;
import com.monobogdan.engine.Mesh;
import com.monobogdan.engine.math.Matrix;
import com.monobogdan.engine.math.Plane;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.world.Component;


public class PlanarShadowCaster implements ShadowCaster {
    private Matrix shadowMatrix = new Matrix();
    private Plane plane = new Plane(Vector.Down, 0.95f);

    private static Material material = new Material("Shadow");

    public PlanarShadowCaster() {
        //material.DepthWrite = false;
        material.R = 0.3f;
        material.G = 0.3f;
        material.B = 0.3f;
        material.A = 0.3f;
        material.AlphaBlend = true;
    }

    public void rebuildMatrix(Matrix baseMatrix, Vector dirLight) {
        shadowMatrix = new Matrix(baseMatrix.Matrix);
        shadowMatrix.shadow(dirLight, plane);
    }

    @Override
    public void onDrawShadow(Mesh mesh, Graphics graphics, Camera camera, int renderPassFlags) {
        graphics.drawMesh(mesh, material, shadowMatrix, camera);
    }
}
