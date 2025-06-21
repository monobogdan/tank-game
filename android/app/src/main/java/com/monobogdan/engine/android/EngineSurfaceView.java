package com.monobogdan.engine.android;

import android.content.Context;
import android.opengl.GLES10;
import android.opengl.GLSurfaceView;
import android.os.Build;

import com.monobogdan.engine.Graphics;
import com.monobogdan.engine.Runtime;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

public class EngineSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private MainActivity activity;

    public EngineSurfaceView(MainActivity activity) {
        super(activity);

        this.activity = activity;

        //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
         //   setEGLContextClientVersion(1);

        setRenderer(this);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        activity.initializeRuntime();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        activity.log("Viewport changed %d %d", width, height);
        activity.Runtime.Graphics.setViewport(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        activity.drawFrame();
    }


}
