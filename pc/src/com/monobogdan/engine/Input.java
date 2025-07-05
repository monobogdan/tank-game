package com.monobogdan.engine;

import com.monobogdan.engine.desktop.Context;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

public class Input extends BaseInput {
    private static Touch touchState = new Touch();

    private int[] keyStates = new int[255];
    private int[] gamePadStates = new int[GamePad.KEY_COUNT];

    private int[] keyMapping = {
        KeyCodes.KEY_LEFT, KeyCodes.KEY_RIGHT, KeyCodes.KEY_UP, KeyCodes.KEY_DOWN, KeyCodes.KEY_SPACE, KeyCodes.KEY_Z, KeyCodes.KEY_X, KeyCodes.KEY_C, KeyCodes.KEY_Q, KeyCodes.KEY_E
    }; // TODO: Move key mapping to configuration

    public Input(Context context) {
        super(context);

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

    @Override
    public int getKeyState(int key) {
        if(key < 0 || key >= keyStates.length)
            throw new IndexOutOfBoundsException("Key code is out of range");

        return keyStates[key];
    }

    @Override
    public int getGamePadState(int gamePadKey) {
        return gamePadStates[gamePadKey];
    }

    public void update() {
        boolean pressed = Mouse.isButtonDown(0);

        if(pressed && touchState.State == STATE_IDLE)
            touchState.State = STATE_PRESSED;

        if(touchState.State == STATE_RELEASED)
            touchState.State = STATE_IDLE;

        if(!pressed && touchState.State == STATE_PRESSED)
            touchState.State = STATE_RELEASED;

        touchState.X = Mouse.getX();
        touchState.Y = Display.getHeight() - Mouse.getY();

        for(int i = 0; i < keyStates.length; i++) {
            pressed = Keyboard.isKeyDown(i);

            if(pressed && keyStates[i] == STATE_IDLE)
                keyStates[i] = STATE_PRESSED;

            if(keyStates[i] == STATE_RELEASED)
                keyStates[i] = STATE_IDLE;

            if(!pressed && keyStates[i] == STATE_PRESSED)
                keyStates[i] = STATE_RELEASED;
        }

        for(int i = 0; i < keyMapping.length; i++) {
            int state = getKeyState(keyMapping[i]);

            gamePadStates[i] = state;
        }
    }
}
