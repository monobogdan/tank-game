package com.monobogdan.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public abstract class BaseInput {
    public static final int STATE_IDLE = 0;
    public static final int STATE_PRESSED = 1;
    public static final int STATE_RELEASED = 2;

    public static final int AXIS_VERTICAL = 0;
    public static final int AXIS_HORIZONTAL = 1;

    public static class Touch {
        public float X;
        public float Y;

        public int State;
    }

    public static class GamePad {
        public static final int KEY_LEFT = 0;
        public static final int KEY_RIGHT = 1;
        public static final int KEY_UP = 2;
        public static final int KEY_DOWN = 3;
        public static final int KEY_A = 4;
        public static final int KEY_X = 5;
        public static final int KEY_Y = 6;
        public static final int KEY_B = 7;
        public static final int KEY_RT = 8;
        public static final int KEY_LT = 9;

        public static final int KEY_COUNT = 10;
    }

    protected Runtime.Platform platform;

    public abstract boolean supportsTouchScreen();
    public abstract Touch getTouchState(int index);
    public abstract int getKeyState(int key);
    public abstract int getGamePadState(int gamePadKey);

    public abstract boolean isKeyPressed(int key);

    protected BaseInput(Runtime.Platform platform) {
        this.platform = platform;
    }

    public float getAxis(int axis) {
        if(axis == AXIS_HORIZONTAL)
            return getGamePadState(GamePad.KEY_LEFT) == STATE_PRESSED ? -1 : (getGamePadState(GamePad.KEY_RIGHT) == STATE_PRESSED ? 1 : 0);
        else
            return getGamePadState(GamePad.KEY_UP) == STATE_PRESSED ? -1 : (getGamePadState(GamePad.KEY_DOWN) == STATE_PRESSED ? 1 : 0);
    }
}
