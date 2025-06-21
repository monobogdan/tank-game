package com.monobogdan.game;

import com.monobogdan.engine.*;
import com.monobogdan.engine.Runtime;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.ui.BitmapFont;
import com.monobogdan.engine.world.World;
import com.monobogdan.engine.world.WorldParser;
import com.monobogdan.game.objects.PlayerTank;
import com.monobogdan.game.world.WorldLoader;

public class Game {
    public Runtime Runtime;

    private BaseGraphics.RenderPass mainPass;

    private Mesh mesh;
    private Texture2D tex;

    private Mesh[] meshes;
    private ResourceThread.AsyncResult loadingResult;

    private BitmapFont font;

    private World world;

    private Vector targetCameraPosition = new Vector(0, 3, -15);
    private Vector targetCameraRotation = new Vector(15, 0, 0);

    private float cameraAnimProgress;

    public Game(Runtime runtime) {
        Runtime = runtime;

        world = new World(runtime);
    }

    public void init() {
        Runtime.Platform.log("Initializing game");

        loadingResult = WorldLoader.Instance.load(Runtime, world, "test");

        tex = Runtime.ResourceManager.getTexture("textures/brick.tex");

        font = BitmapFont.load(Runtime, "font/default.font");
    }

    public void update() {
        if(loadingResult != null && loadingResult.isDone()) {
            if(!loadingResult.isSuccessful())
                throw new RuntimeException("Loading task cancelled due to exception");
            else
                loadingResult = null;
        }

        world.update();
    }

    public void draw() {
        world.draw();

        //Runtime.Graphics.Canvas.drawString(font,  Vector.One, 0, 0, 16, "Всем привет, я люблю пиво!!!");
    }

    public void beforeClose() {

    }
}
