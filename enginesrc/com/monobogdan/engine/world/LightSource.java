package com.monobogdan.engine.world;

import com.monobogdan.engine.BaseGraphics;

import java.util.HashMap;

public class LightSource extends GameObject {
    public BaseGraphics.Light Light = new BaseGraphics.Light();
    public boolean UpdatedSinceLastFrame = true;

    private int lightId;

    public LightSource() {

    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onUpdate() {

    }

    @Override
    public void onDestroy() {

    }
}
