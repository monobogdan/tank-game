package com.monobogdan.game;

import com.monobogdan.engine.*;
import com.monobogdan.engine.Runtime;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.Mesh;
import com.monobogdan.engine.Texture2D;
import com.monobogdan.engine.world.World;
import com.monobogdan.game.objects.PlayerTank;
import com.monobogdan.game.ui.TouchGamepad;
import com.monobogdan.game.world.WorldLoader;

public class Game {
    public Runtime Runtime;
    public PlayerTank Player;
    public TouchGamepad TouchGamepad; // TODO: Split input manager
    public InputManager InputManager;

    private Mesh mesh;
    private Texture2D tex;

    private Mesh[] meshes;
    private ResourceThread.AsyncResult loadingResult;

    private World world;

    private Vector targetCameraPosition = new Vector(0, 3, -15);
    private Vector targetCameraRotation = new Vector(15, 0, 0);

    private float cameraAnimProgress;

    public Game(Runtime runtime) {
        Runtime = runtime;

        world = new World(runtime);
        TouchGamepad = new TouchGamepad(this);

        InputManager = new InputManager(this);
    }

    public void init() {
        Runtime.Platform.log("Initializing game");

        loadingResult = WorldLoader.Instance.load(this, world, "test");

        tex = Runtime.ResourceManager.getTexture("textures/brick.tex");
    }

    public void update() {
        if(loadingResult != null && loadingResult.isDone()) {
            if(loadingResult.isSuccessful())
                loadingResult = null;
        }

        world.update();
    }

    public void drawUI() {
        TouchGamepad.drawUI();
    }

    public void draw() {
        world.draw();

        //Runtime.Graphics.Canvas.drawString(font,  Vector.One, 0, 0, 16, "Всем привет, я люблю пиво!!!");
    }

    public void beforeClose() {

    }
}
