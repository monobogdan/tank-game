package com.monobogdan.engine;

import com.monobogdan.engine.math.Matrix;
import com.monobogdan.engine.math.Plane;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.world.GameObject;
import com.monobogdan.engine.world.components.MeshRenderer;

public class Frustum {
    private Plane[] planes = new Plane[6];

    public Frustum() {
        for(int i = 0; i < planes.length; i++) {
            planes[i] = new Plane();
        }
    }

    public void calculate(Matrix viewProj) {
        float[] items = viewProj.Matrix;
        planes[0].set(items[3] - items[0], items[7] - items[4], items[11] - items[8], items[15] - items[12]).normalize();
        planes[1].set(items[3] + items[0], items[7] + items[4], items[11] + items[8], items[15] + items[12]).normalize();
        planes[2].set(items[3] + items[1], items[7] + items[5], items[11] + items[9], items[15] + items[13]).normalize();
        planes[3].set(items[3] - items[1], items[7] - items[5], items[11] - items[9], items[15] - items[13]).normalize();
        planes[4].set(items[3] - items[2], items[7] - items[6], items[11] - items[10], items[15] - items[14]).normalize();
        planes[5].set(items[3] + items[2], items[7] + items[6], items[11] + items[10], items[15] + items[14]).normalize();
    }

    // Allocation-less
    public boolean isPointInFrustum(float x, float y, float z)
    {
        for(int i = 0; i < planes.length; i++)
        {
            Plane plane = planes[i];

            if ((plane.A * x) + (plane.B * y) + (plane.C * z) + plane.D <= 0)
                return false;
        }

        return true;
    }

    public boolean isMeshRendererInFrustum(MeshRenderer renderer) {
        float x = renderer.Parent.Position.X;
        float y = renderer.Parent.Position.Y;
        float z = renderer.Parent.Position.Z;
        Vector min = renderer.Mesh.BoundingMin;
        Vector max = renderer.Mesh.BoundingMax;

        return isPointInFrustum(x + min.X, -(y + min.Y), z + min.Z) || isPointInFrustum(x + max.X, -(y + max.Y), z + max.Z);
    }
}
