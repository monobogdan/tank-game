package com.monobogdan.engine;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class ResourceManager {
    private HashMap<String, WeakReference<Object>> loadedObjects;

    private Runtime runtime;

    ResourceManager(Runtime runtime) {
        this.runtime = runtime;

        runtime.Platform.log("Initializing resource manager");
        loadedObjects = new HashMap<String, WeakReference<Object>>(1024);
    }

    private Object getNamedObject(String name, Class expectedClass) {
        if(loadedObjects.containsKey(name)) {
            WeakReference weakRef = loadedObjects.get(name);
            Object obj = weakRef.get();

            if(obj == null) {
                runtime.Platform.log("[Resources] Object '%s' was freed previously. Reloading..."); // TODO: Implement weak references removal over time
                return null;
            }

            if(obj.getClass() != expectedClass)
                throw new ClassCastException("Object of name " + name + " is instance of " + obj.getClass().getSimpleName() + ", but getNamedObject expected " + expectedClass.getSimpleName());

            return obj;
        }

        return null;
    }

    private void addObjectToPool(String name, Object obj) {
        loadedObjects.put(name, new WeakReference<Object>(obj));
    }

    public Texture2D getTexture(String name) {
        Texture2D tex = (Texture2D) getNamedObject(name, Texture2D.class);

        if(tex == null) {
            tex = TextureLoader.load(runtime, name);
            addObjectToPool(name, tex);
        }

        return tex;
    }

    public Mesh getMesh(String name) {
        Mesh mesh = (Mesh) getNamedObject(name, Mesh.class);

        if(mesh == null) {
            mesh = MeshLoader.load(runtime, name);
            addObjectToPool(name, mesh);
        }

        return mesh;
    }

    public Material getMaterial(String name) {
        Material material = (Material) getNamedObject(name, Material.class);

        if(material == null) {
            material = MaterialLoader.load(runtime, name);
            addObjectToPool(name, material);
        }

        return material;
    }
}
