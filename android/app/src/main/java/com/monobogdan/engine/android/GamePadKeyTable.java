package com.monobogdan.engine.android;

import android.view.KeyEvent;

/**
 * Created by mono on 29.06.2025.
 */

/***
 * Linear key translation table.
 *
 * Translates from standard Android layouts (i.e Xperia Play, default gamepad, emulator consoles and QWERTY keyboards) to GamePad keys.
 */
public class GamePadKeyTable {
    /*
        public static final int KEY_LEFT = 0;
        public static final int KEY_RIGHT = 1;
        public static final int KEY_UP = 2;
        public static final int KEY_DOWN = 3;
        public static final int KEY_A = 4;
        public static final int KEY_X = 5;
        public static final int KEY_Y = 6;
        public static final int KEY_B = 7;
     */

    private static int[] xperiaPlayMapping = {
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_BUTTON_X, KeyEvent.KEYCODE_BUTTON_Y, KeyEvent.KEYCODE_BUTTON_R1, KeyEvent.KEYCODE_BUTTON_L1
    };

    private static int[] genericQWERTYMapping = {
            KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_S, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_J, KeyEvent.KEYCODE_K,
            KeyEvent.KEYCODE_Q, KeyEvent.KEYCODE_E
    };

    public static int[][] ConversionTable = {
        xperiaPlayMapping,
        genericQWERTYMapping
    };
}
