package com.monobogdan.engine.android;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import com.monobogdan.engine.Graphics;
import com.monobogdan.engine.Input;
import com.monobogdan.engine.Runtime;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mono on 06.06.2025.
 */

public class MainActivity extends Activity implements Runtime.Platform {
    private static final String TAG = "Context";

    Runtime Runtime;
    private EngineSurfaceView glView;

    private Graphics graphics;
    private Input input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        log("Java version: %s", System.getProperty("java.version"));

        glView = new EngineSurfaceView(this);
        setContentView(glView);
    }

    void initializeRuntime() {
        log("Initialization started");

        graphics = new Graphics(this);
        input = new Input();

        Runtime = new Runtime(this);
        Runtime.init();
    }

    void drawFrame() {
        Runtime.update();
        Runtime.draw();
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
