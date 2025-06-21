package com.monobogdan.engine;

/**
 * Created by mono on 18.06.2025.
 */

public class Input {
    public static class Touch {
        public float X;
        public float Y;

        public boolean IsPressed;
    }

    private static Touch touchState = new Touch();

    public Input() {

    }

    public boolean supportsTouchScreen() {
        return true;
    }

    public Touch getTouchState(int index) {
        if(index < 0)
            throw new RuntimeException("Incorrect touch index");

        return index == 0 ? touchState : null;
    }

    public boolean isKeyPressed(int key) {
        return false;
    }

    void update() {

    }
}
