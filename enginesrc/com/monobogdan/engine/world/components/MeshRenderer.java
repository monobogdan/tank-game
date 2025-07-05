package com.monobogdan.engine.world.components;

import com.monobogdan.engine.*;
import com.monobogdan.engine.math.Matrix;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.world.Component;

public class MeshRenderer extends Renderer {
    public Mesh Mesh;
    public com.monobogdan.engine.Material Material;

    public boolean CanBeOccluded = true;
    public boolean CastsShadow;
    public ShadowCaster ShadowCaster;

    public Matrix Matrix = new Matrix();

    public MeshRenderer() {
        CastsShadow = true;
    }

    public boolean isInFrustum(Camera camera) {
        return true; // TODO: Not implemented yet
    }

    public void setTransform(Vector position, Vector rotation) {
        // Recalculate matrix
        Matrix.identity();
        Matrix.translate(position.X, position.Y, position.Z);
        Matrix.rotateY(rotation.Y);
        Matrix.rotateX(rotation.X);
        Matrix.rotateZ(rotation.Z);
    }

    public void setTransform(Matrix matrix) {
        this.Matrix = matrix;
    }

    @Override
    public void onUpdate() {

    }

    @Override
    public void onDraw(Graphics graphics, Camera camera, int renderPassFlags) {
        if(Mesh != null && Material != null) {
            if(CastsShadow && ShadowCaster != null)
                ShadowCaster.onDrawShadow(Mesh, graphics, camera, renderPassFlags);

            if(!CanBeOccluded || camera.Frustum.isMeshRendererInFrustum(this))
                graphics.drawMesh(Mesh, Material, Matrix, camera);
        }
    }
}
