package com.monobogdan.engine;

import com.monobogdan.engine.ui.DebugUI;
import com.monobogdan.engine.ui.UI;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mono on 06.06.2025.
 */

public class Runtime {
    public interface Platform {
        String getName();

        com.monobogdan.engine.Graphics getGraphics();
        Input getInput();

        void log(String fmt, Object... args);
        void logException(Throwable exception);
        InputStream openFile(String fileName) throws IOException;
        void requestExit();
    }

    public static final int ENGINE_VERSION = 100;

    Thread MainThread = Thread.currentThread();

    public Platform Platform;
    public com.monobogdan.engine.Graphics Graphics;
    public Input Input;
    public ResourceManager ResourceManager;
    public TaskScheduler Scheduler;
    public Time Time;
    public UI UI;

    public com.monobogdan.game.Game Game;
    private DebugUI debugUI;

    // Game is not supplied if we in editor mode
    public Runtime(Platform platform) {
        platform.log("Engine initialization started");
        platform.log("Platform: %s", platform.getName());

        Platform = platform;

        Graphics = platform.getGraphics();
        Input = platform.getInput();
        if(Graphics == null)
            throw new NullPointerException("Graphics is null");

        Scheduler = new TaskScheduler(this);
        Time = new Time();
        ResourceManager = new ResourceManager(this);
        UI = new UI(this);

        platform.log("Initialization succeed");

        this.Game = new com.monobogdan.game.Game(this);
        debugUI = new DebugUI(this);
    }

    public void init() {
        Game.init();
    }

    public void beginFrame() {
        Time.update();
    }

    public void endFrame() {
        Time.endUpdate();
    }

    public void update() {
        Scheduler.update(); // Do main thread jobs

        Game.update();
    }

    public void draw() {
        Game.draw();
        Game.drawUI();
        debugUI.draw();
        Graphics.FrameStatistics.reset(); // TODO: Move to some BeginScene/EndScene
    }

    // Do not call directly
    public void releaseResources() {
        ResourceThread.staticFinalize();;
    }
}
