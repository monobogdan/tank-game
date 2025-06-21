package com.monobogdan.engine;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class Input {
    public static class Touch {
        public float X;
        public float Y;

        public boolean IsPressed;
    }

    private static Touch touchState = new Touch();

    public Input() {
        try {
            Keyboard.create();
            Mouse.create();

            touchState = new Touch();
        } catch (LWJGLException e) {
            throw new RuntimeException("Failed to initialize input module", e);
        }
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
        return Keyboard.isKeyDown(key);
    }

    void update() {
        touchState.IsPressed = Mouse.isButtonDown(0);
        touchState.X = Mouse.getX();
        touchState.Y = Mouse.getY();
    }
}
