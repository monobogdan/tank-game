package com.monobogdan.engine.world;

import com.monobogdan.engine.Camera;
import com.monobogdan.engine.Graphics;
import com.monobogdan.engine.math.Vector;

import java.lang.reflect.Modifier;

public abstract class GameObject {
    public World World;

    public String Name;
    public Vector Position;

    public java.util.Vector<Component> components;

    public GameObject() {
        Position = new Vector(0, 0, 0);
        Name = toString();

        components = new java.util.Vector<Component>(4); // Usually it's not bigger than 4 components per gameobject
    }

    public void attachToWorld(World world) {
        if(world == null)
            throw new AssertionError("World can't be null");

        // Breaks RAII?
        this.World = world;
    }

    public <T extends Component> T attachComponent(Class<T> clazz) {
        Component c = getComponent(clazz);
        if(c != null)
            throw new RuntimeException("Attempt to attach component " + clazz.getSimpleName() + " on GameObject " + this + " while component is already present");

        if(Modifier.isAbstract(clazz.getModifiers()))
            throw new RuntimeException("Attempt to attach abstract component " + clazz.getSimpleName() + " on GameObject " + this);

        try {
            T t = (T) clazz.newInstance();
            t.attachToGameObject(this);

            components.add(t);
            return t;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to attach component " + clazz.getSimpleName() + " (not public constructor)", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Component " + clazz.getSimpleName() + " constructor has thrown an exception", e);
        }
    }

    public <T extends Component> T getComponent(Class<T> clazz) {
        for(Component c : components) {
            if(clazz.isAssignableFrom(c.getClass()))
                return (T)c;
        }

        return null;
    }

    public <T extends Component> void removeComponent(Class<T> clazz) {
        if(components.contains(clazz))
            components.remove(clazz);
        else
            World.Runtime.Platform.log("Component '%s' doesn't exist on gameobject '%s'", clazz.getSimpleName(), this);
    }

    public void onCreate() {

    }

    public void onUpdate() {
        for(Component c : components)
            c.onUpdate();
    }

    public void onDraw(Graphics graphics, Camera camera, int renderPassFlags) {
        for(Component c : components) {
            c.onDraw(graphics, camera, renderPassFlags);
        }
    }

    public void onDestroy() {

    }

    public void loadResources() {

    }

    public void onLateUpdate() {

    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
