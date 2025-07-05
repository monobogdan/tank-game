package com.monobogdan.game.objects;

import com.monobogdan.engine.Material;
import com.monobogdan.engine.math.MathUtils;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.world.StaticMesh;

public class Bullet extends StaticMesh {
    public static final float TARGET_VELOCITY = 0.7f;

    private float velocity;
    private Vector forward = new Vector();

    public Bullet() {
        super(false);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        MeshRenderer.Mesh = World.Runtime.ResourceManager.getMesh("mesh/bullet.mdl");
        MeshRenderer.Material = Material.createDiffuse(World.Runtime, "bullet_primary", World.Runtime.ResourceManager.getTexture("textures/bullet.tex")); // TODO: CompiledMaterial re-usage
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        velocity = TARGET_VELOCITY;
        forward.calculateForward(Rotation);
        forward.multiply(velocity);

        Position.add(forward);
    }
}
