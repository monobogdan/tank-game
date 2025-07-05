package com.monobogdan.engine.world.components;

import com.monobogdan.engine.Camera;
import com.monobogdan.engine.Graphics;

public class BatchedMeshRenderer extends MeshRenderer {
    public boolean IsTakenByBatcher = false;

    @Override
    public void onDraw(Graphics graphics, Camera camera, int renderPassFlags) {
        if(!IsTakenByBatcher)
            super.onDraw(graphics, camera,  renderPassFlags);
    }
}
