package com.monobogdan.engine.world.components;

import com.monobogdan.engine.Camera;
import com.monobogdan.engine.Graphics;
import com.monobogdan.engine.Mesh;

public interface ShadowCaster {
    void onDrawShadow(Mesh mesh, Graphics graphics, Camera camera, int renderPassFlags);
}
