package com.monobogdan.engine.desktop;

import com.monobogdan.engine.Graphics;
import com.monobogdan.engine.Input;
import com.monobogdan.engine.Runtime;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.*;
import org.lwjgl.opengl.DisplayMode;

import java.io.*;
import java.util.Properties;

public class Context implements com.monobogdan.engine.Runtime.Platform, Thread.UncaughtExceptionHandler {
    public static String LOG_FILENAME = "game.log";

    private String dataFolder;
    private Log log;

    private Graphics graphics;
    private Input input;

    public Runtime Runtime;

    public Context(String dataFolder) throws RuntimeException {
        log = new Log(LOG_FILENAME);

        if(dataFolder == null)
            throw new RuntimeException("No data folder specified");

        this.dataFolder = dataFolder;

        log("Context startup");
        log("JVM version: %s", System.getProperty("java.version"));
        log("JVM vendor: %s", System.getProperty("java.vm.name"));
        log("CPU: %s", System.getProperty("os.arch"));

        log("Installing main thread exception handler");
        Thread.currentThread().setUncaughtExceptionHandler(this);

        try {
            createWindow();
        } catch (LWJGLException e) {
            log("Something went wrong with LWJGL");

            throw new RuntimeException(e);
        }

        graphics = new Graphics(this);
        input = new Input(this);

        Runtime = new Runtime(this);
    }

    private void createWindow() throws LWJGLException {
        DisplayMode mode = new DisplayMode(800, 600);

        log("Creating window with size %dx%d", mode.getWidth(), mode.getHeight());

        Display.create(new PixelFormat(), new ContextAttribs(1, 5));
        Display.setDisplayMode(mode);
        Display.setTitle("GameTests engine");
        Display.setResizable(false);
        Display.setVSyncEnabled(true);
        Display.makeCurrent();
    }

    public void run() {
        log("Starting main loop");

        Runtime.init();
        while(!Display.isCloseRequested()) {
            Runtime.beginFrame();
            Display.processMessages();

            Runtime.Input.update();
            Runtime.Graphics.setViewport(Display.getWidth(), Display.getHeight());

            Runtime.update();
            Runtime.draw();

            try {
                Display.swapBuffers();
            } catch (LWJGLException e) {
                log("SwapBuffers failed");
            }
            Runtime.endFrame();
        }

        Runtime.releaseResources();
        log("Window is closed");
    }

    @Override
    public String getName() {
        return System.getProperty("os.name");
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
    public void log(String fmt, Object... args) {
        log.print(fmt, args);
    }

    @Override
    public void logException(Throwable exception) {
        log.printException(exception);
    }

    @Override
    public void requestExit() {
        //Display.destroy();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        StringWriter strWriter = new StringWriter();
        PrintWriter alertPrintWriter = new PrintWriter(strWriter);

        log.printException(e);
        e.printStackTrace(alertPrintWriter);

        Sys.alert("Uncaught exception on main thread ", strWriter.getBuffer().toString());

        Display.destroy();
        System.exit(-1);
    }

    @Override
    public InputStream openFile(String fileName) throws IOException {
        return new FileInputStream(dataFolder + fileName);
    }
}
