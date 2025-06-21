package com.monobogdan.engine.world;

import com.monobogdan.engine.Runtime;
import com.monobogdan.engine.scripting.ScriptClassLoader;

import java.io.IOException;
import java.util.*;

public final class ObjectRegistry {
    private static Class[] BuiltInGameObjects = {
      LightSource.class,
      StaticMesh.class
    };

    public static final String SCRIPTS_FILENAME = "scripts.jar";

    private Runtime runtime;

    private HashMap<String, Class> classMap;
    private ScriptClassLoader classLoader;

    public ObjectRegistry(Runtime runtime) {
        this.runtime = runtime;

        reloadScriptJar();
    }

    private void putStandardObjects() {
        for(Class _class : BuiltInGameObjects)
            classMap.put(_class.getSimpleName(), _class);
    }

    public void reloadScriptJar() {
        classMap = new HashMap<String, Class>();

        putStandardObjects();

        /*
        runtime.Platform.log("Loading scripts library \"%s\"", SCRIPTS_FILENAME);

        try {
            classLoader = new ScriptClassLoader(SCRIPTS_FILENAME);
            ExportedObjects objs  = (ExportedObjects)classLoader.loadClass("ScriptExports").newInstance();
            List<Class> classes = objs.getExportedObjectsClasses();

            // Verify
            runtime.Platform.log("Verifying exported classes");

            classMap = new HashMap<String, Class>();

            putStandardObjects();
            for(Class _class : classes) {
                if(!GameObject.class.isAssignableFrom(_class))
                    throw new RuntimeException("Verification failed: Class " + _class.getName() + " is not inherited from GameObject");

                if(classMap.containsKey(_class.getSimpleName()))
                    throw new RuntimeException("GameObject class is already registered " + _class.getSimpleName() + ". Please note that they are indexed by their simple names, without package.");
                classMap.put(_class.getSimpleName(), _class);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to load scripting library", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Script library doesn't have ScriptExports class", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("ScriptExports shouldn't be private", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Can't instantiate ScriptExports class", e);
        }*/
    }

    public Collection<Class> getClasses() {
        return classMap.values();
    }

    public Class getGameObjectClass(String name) throws ClassNotFoundException {
        Class ret = classMap.get(name);

        if(ret == null)
            throw new ClassNotFoundException("Script library doesn't export class " + name);

        return ret;
    }
}
