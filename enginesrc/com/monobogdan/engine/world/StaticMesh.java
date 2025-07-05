package com.monobogdan.engine.world;

import com.monobogdan.engine.*;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.Graphics;
import com.monobogdan.engine.world.components.BatchedMeshRenderer;
import com.monobogdan.engine.world.components.MeshRenderer;

public class StaticMesh extends GameObject {
    protected MeshRenderer MeshRenderer;

    public Vector Rotation = new Vector();
    public Vector Scale = new Vector(1, 1, 1);

    private Vector oldRotation = new Vector(999, 999, 999), oldPosition = new Vector(-999, -999, 999);

    public StaticMesh(boolean canBeBatched) {
        super();

        if(!canBeBatched)
            MeshRenderer = attachComponent(com.monobogdan.engine.world.components.MeshRenderer.class);
        else
            MeshRenderer = attachComponent(BatchedMeshRenderer.class);
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

        if(!Position.compare(oldPosition) || !Rotation.compare(oldRotation))
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
