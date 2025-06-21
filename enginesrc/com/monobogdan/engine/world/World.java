package com.monobogdan.engine.world;

import com.monobogdan.engine.*;
import com.monobogdan.engine.Runtime;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class World {
    private static final int WORLD_HEADER = 0x1337;

    private static HashMap<String, String> hashMapCache = new HashMap(32);

    private Queue<GameObject> spawnQueue;
    private Queue<GameObject> removalQueue;
    private ArrayList<LightSource> contributingLights;
    private ObjectRegistry objectRegistry;

    ArrayList<BaseGraphics.RenderPass> worldRenderPasses;

    // Public fields
    public Runtime Runtime;
    public HashMap<String, String> Information; // Meta-data
    public Camera Camera;
    public Vector<GameObject> GameObjects;

    // Package-private fields
    public int LightCounter;

    public World(Runtime runtime) {
        this.Runtime = runtime;

        Camera = new Camera();

        objectRegistry = new ObjectRegistry(runtime);
        GameObjects = new Vector<GameObject>(1024);
        spawnQueue = new ArrayDeque<GameObject>(8);
        removalQueue = new ArrayDeque<GameObject>(8);
        contributingLights = new ArrayList<LightSource>(8);

        Information = new HashMap<String, String>();
        Information.put("sky", "default");

        setupRenderPasses();
    }

    public GameObject spawn(GameObject obj) {
        if(obj == null)
            return null;

        if(obj instanceof LightSource) {
            if(contributingLights.size() == 8)
                Runtime.Platform.log("LightSource %s will not contribute into scene. Limit is reached.", obj);
            else
                contributingLights.add((LightSource)obj);
        }

        obj.attachToWorld(this);
        GameObjects.add(obj);

        obj.onCreate();

        return obj;
    }

    public void remove(GameObject obj) {
        if(obj == null || !GameObjects.contains(obj) || removalQueue.contains(obj))
            return;

        if(obj instanceof LightSource)
            contributingLights.remove(obj);

        obj.onDestroy();
        removalQueue.add(obj);
    }

    public ObjectRegistry getObjectRegistry() {
        return objectRegistry;
    }

    public void update() {
        Camera.updateProjection();
        Camera.calculateVectors();

        for(GameObject obj : GameObjects)
            obj.onUpdate();

        // Second pass for late objects
        for(GameObject obj : GameObjects)
            obj.onLateUpdate();

        GameObjects.addAll(spawnQueue); // Thread safety?
        GameObjects.removeAll(removalQueue);
        spawnQueue.clear();
        removalQueue.clear();
    }

    public void draw() {
        // Light pre-pass
        for(int i = 0; i < contributingLights.size(); i++)
            Runtime.Graphics.setLightSource(i, contributingLights.get(i).Light);

        for(BaseGraphics.RenderPass pass : worldRenderPasses)
            Runtime.Graphics.doPass(pass.getClass().getSimpleName(), pass);
    }

    private void setupRenderPasses() {
        worldRenderPasses = new ArrayList<BaseGraphics.RenderPass>();

        worldRenderPasses.add(new BaseGraphics.RenderPass() {
            @Override
            public void onRender(Graphics graphics, String passName) {
                graphics.clear(0, 0, 1);

                for(GameObject obj : GameObjects)
                    obj.onDraw(graphics, Camera, 0);
            }
        });
    }
}
