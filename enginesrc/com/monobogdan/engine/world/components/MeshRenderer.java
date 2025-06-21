package com.monobogdan.engine.world.components;

import com.monobogdan.engine.Camera;
import com.monobogdan.engine.Graphics;
import com.monobogdan.engine.Material;
import com.monobogdan.engine.Mesh;
import com.monobogdan.engine.math.Matrix;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.world.Component;

public class MeshRenderer extends Component {
    public Mesh Mesh;
    public Material Material;

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

            graphics.drawMesh(Mesh, Material, Matrix, camera);
        }
    }
}
