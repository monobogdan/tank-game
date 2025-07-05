package com.monobogdan.game.world;

import com.monobogdan.engine.ResourceThread;
import com.monobogdan.engine.Runtime;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.world.*;
import com.monobogdan.game.Game;
import com.monobogdan.game.objects.PlayerTank;
import com.monobogdan.game.objects.Point;
import com.monobogdan.game.objects.StaticObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class WorldLoader {
    public static WorldLoader Instance;

    static {
        Instance = new WorldLoader();
    }

    private WorldLoader() { }

    public ResourceThread.AsyncResult load(final Game game, final World world, final String worldName) {
        game.Runtime.Platform.log("Loading world \"%s\"", worldName);

        return ResourceThread.start(game.Runtime, new ResourceThread.LoadingWorker() {
            @Override
            public void onBeforeLoad(ResourceThread.AsyncResult res) {

            }

            private void checkParameterCount(String object, String[] values, int needed) {
                if(values.length < needed)
                    throw new RuntimeException("Object " + object + " expected " + needed + " parameters but got only " + values.length);
            }

            public GameObject spawnGameObject(String obj, Vector position, Vector rotation, String[] values) {
                GameObject ret = null;

                if(obj.equals("Point")) {
                    checkParameterCount(obj, values, 1);

                    ret = new Point(values[0]);
                }

                if(obj.equals("StaticObject")) {
                    checkParameterCount(obj, values, 2);

                    ret = new StaticObject(values[0].equals("1"), values[3].equals("1"), values[1], values[2]);
                }

                if(ret != null)
                    ret.Position = position;

                return ret;
            }

            @Override
            public void onLoad(ResourceThread.AsyncResult res) {
                try {
                    InputStream strm = game.Runtime.Platform.openFile("maps/" + worldName + ".map");

                    res.setProgressStage("parsing");
                    res.setProgress(0);
                    game.Runtime.Platform.log("Entered state: Parsing");

                    final ArrayList<GameObject> objects = new ArrayList<GameObject>();

                    WorldParser.parse(strm, new WorldParser.ParserImplementation() {
                        @Override
                        public void processTag(String tag, String value) {
                            game.Runtime.Platform.log("Tag %s %s", tag, value);
                        }

                        @Override
                        public void processGameObject(String obj, Vector position, Vector rotation, String[] values) {
                            GameObject gameObject = spawnGameObject(obj, position, rotation, values);
                            if(gameObject == null) {
                                game.Runtime.Platform.log("Unknown GameObject %s", obj);
                                return;
                            }

                            gameObject.attachToWorld(world);
                            objects.add(gameObject);
                        }
                    });

                    res.setProgressStage("loadingAssets");
                    res.setProgress(30);
                    game.Runtime.Platform.log("Entered state: Loading assets");

                    int totalObjects = objects.size();
                    int currObject = 0;

                    for(GameObject obj : objects) {
                        currObject++;
                        obj.loadResources();

                        res.setProgress(30 + currObject);
                    }

                    // Spawn light source
                    LightSource sun = new LightSource();
                    sun.Light.IsDirectional = true;
                    sun.Light.Ambient = Vector.fromColor(128, 128, 128);
                    sun.Light.Diffuse = Vector.fromColor(253, 251, 211);
                    sun.Light.Position = new Vector(0.5f, 0.2f, 0.3f);

                    // Spawz`n player
                    game.Player = new PlayerTank();
                    game.Player.attachToWorld(world);
                    game.Player.Position = new Vector(0, 0, -10);
                    game.Player.loadResources();
                    objects.add(game.Player);
                    objects.add(sun);

                    game.Runtime.Scheduler.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            for(GameObject obj : objects)
                                world.spawn(obj);

                            world.BatchManager.bake();
                        }
                    });

                    game.Runtime.Platform.log("Entered state: Done");

                    strm.close();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to open map file " + worldName, e);
                }
            }
        }, "World" + worldName);
    }
}
