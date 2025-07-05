package com.monobogdan.engine.world;

import com.monobogdan.engine.Camera;
import com.monobogdan.engine.Graphics;

public abstract class Component {
    public GameObject Parent;

    public boolean Active;

    public Component() {
        Active = true;
    }

    void attachToGameObject(GameObject obj) {
        if(obj == null)
            throw new NullPointerException("GameObject can't be null for component " + this);

        Parent = obj;
    }

    public void onUpdate() {

    }

    public void onDraw(Graphics graphics, Camera camera, int renderPassFlags) {
        
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
