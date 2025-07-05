package com.monobogdan.engine;

/**
 * Created by mono on 18.06.2025.
 */

public class Input extends BaseInput {
    private Touch[] touches = new Touch[10];
    private int[] keyStates = new int[256];
    private int[] gamePadStates = new int[GamePad.KEY_COUNT];

    public Input(Runtime.Platform platform) {
        super(platform);

        for(int i = 0; i < touches.length; i++)
            touches[i] = new Touch();
    }

    public boolean supportsTouchScreen() {
        return true;
    }

    public Touch getTouchState(int index) {
        if(index < 0)
            throw new RuntimeException("Incorrect touch index");

        return index < touches.length ? touches[index] : null;
    }

    public void updateKeyState(int key, int state) {
        if(key >= 0 && key < keyStates.length)
            keyStates[key] = state;
    }

    public void updateGamePadState(int key, int state) {
        if(key >= 0 && key < gamePadStates.length)
            gamePadStates[key] = state;
    }

    public boolean isKeyPressed(int key) {
        return keyStates[key] == STATE_PRESSED;
    }

    @Override
    public int getKeyState(int key) {
        if(key < 0 || key >= keyStates.length)
            throw new IndexOutOfBoundsException("Key code is out of range");

        return keyStates[key];
    }

    @Override
    public int getGamePadState(int gamePadKey) {
        if(gamePadKey < 0 || gamePadKey >= gamePadStates.length)
            throw new IndexOutOfBoundsException("GamePad key code is out of range");

        return gamePadStates[gamePadKey];
    }

    void update() {
        for(int i = 0; i < keyStates.length; i++) {
            if(keyStates[i] == STATE_RELEASED)
                keyStates[i] = STATE_IDLE;
        }
    }
}
