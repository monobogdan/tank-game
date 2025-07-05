package com.monobogdan.engine.android;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.monobogdan.engine.BaseInput;
import com.monobogdan.engine.Graphics;
import com.monobogdan.engine.Input;
import com.monobogdan.engine.Runtime;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mono on 06.06.2025.
 */

public class MainActivity extends Activity implements Runtime.Platform {
    class InputListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            BaseInput.Touch touch = input.getTouchState(motionEvent.getActionIndex());

            if(touch == null)
                return false; // Touch >= 10

            int action = motionEvent.getAction();

            if(action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_DOWN) {
                touch.State = BaseInput.STATE_PRESSED;
                touch.X = motionEvent.getX();
                touch.Y = motionEvent.getY();
            }

            if(action == MotionEvent.ACTION_UP)
                touch.State = BaseInput.STATE_IDLE;

            return true;
        }
    }

    private static final String TAG = "Context";

    Runtime Runtime;
    private EngineSurfaceView glView;

    private Graphics graphics;
    private Input input;

    private InputListener inputListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        log("Java version: %s", System.getProperty("java.version"));

        inputListener = new InputListener();

        glView = new EngineSurfaceView(this);
        glView.setOnTouchListener(inputListener);
        setContentView(glView);
    }

    private void handleKeyEvent(int keyCode, int state) {
        input.updateKeyState(keyCode, state);
    }

    private void handleGamePadEvent(int key, int state) {
        input.updateGamePadState(key, state);
    }

    private int resolveGamePadTranslationTable(int keyCode) {
        for(int i = 0; i < GamePadKeyTable.ConversionTable.length; i++) {
            int[] keys = GamePadKeyTable.ConversionTable[i];

            for(int j = 0; j < keys.length; j++) {
                if(keyCode == keys[j])
                    return j;
            }
        }

        return -1; // Not resolved
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        int gamePadKey = resolveGamePadTranslationTable(keyCode);
        handleKeyEvent(event.getScanCode(), Input.STATE_RELEASED);

        if(gamePadKey != -1)
            handleGamePadEvent(gamePadKey, Input.STATE_RELEASED);

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int gamePadKey = resolveGamePadTranslationTable(keyCode);
        handleKeyEvent(event.getScanCode(), Input.STATE_PRESSED);

        if(gamePadKey != -1)
            handleGamePadEvent(gamePadKey, Input.STATE_PRESSED);

        return true;
    }

    void initializeRuntime() {
        log("Initialization started");

        graphics = new Graphics(this);
        input = new Input(this);

        Runtime = new Runtime(this);
        Runtime.init();
    }

    void drawFrame() {
        Runtime.beginFrame();
        Runtime.update();
        Runtime.draw();
        Runtime.endFrame();
    }

    // OS interface implementation
    @Override
    public String getName() {
        return "Android";
    }

    @Override
    public void log(String fmt, Object... args)
    {
        Log.i(TAG, String.format(fmt, args));
    }

    @Override
    public Graphics getGraphics() {
        return graphics;
    }

    @Override
    public Input getInput() {
        return input;
    }

    @Override
    public void requestExit() {
        finish();
    }

    @Override
    public void logException(Throwable exception) {
        Log.e("ENGINE_EXCEPTION", "An exception occurred", exception);
    }

    @Override
    public InputStream openFile(String fileName) throws IOException {
        return getAssets().open(fileName, AssetManager.ACCESS_RANDOM);
    }
}
