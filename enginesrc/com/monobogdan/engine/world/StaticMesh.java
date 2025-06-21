package com.monobogdan.engine.world;

import com.monobogdan.engine.*;
import com.monobogdan.engine.math.Matrix;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.world.components.MeshRenderer;

public class StaticMesh extends GameObject {
    protected MeshRenderer MeshRenderer;

    public Vector Rotation = new Vector();
    public Vector Scale = new Vector(1, 1, 1);

    public StaticMesh() {
        MeshRenderer = attachComponent(MeshRenderer.class);
    }

    @Override
    public void loadResources() {
        super.loadResources();


    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        MeshRenderer.setTransform(Position, Rotation);
    }

    @Override
    public void onDraw(Graphics graphics, Camera camera, int renderPassFlags) {
        super.onDraw(graphics, camera, renderPassFlags);
    }

    @Override
    public void onDestroy() {

    }
}
