package com.monobogdan.game.objects;

import com.monobogdan.engine.KeyCodes;
import com.monobogdan.engine.Material;
import com.monobogdan.engine.Mesh;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.world.StaticMesh;
import com.monobogdan.engine.world.components.CollisionHolder;
import com.monobogdan.engine.world.components.PlanarShadowCaster;

public class PlayerTank extends StaticMesh {
    public static final float MAX_SPEED = 1.0f;
    public static final float ACCELERATION_FACTOR = 0.1f;
    public static final float DECELERATION_FACTOR = 0;

    private CollisionHolder collisionHolder;

    private Vector rot0 = new Vector(0, 0, 0);
    private Vector rot90 = new Vector(0, -90, 0);
    private Vector rot180 = new Vector(0, -180, 0);
    private Vector rot270 = new Vector(0, -270, 0);

    private Vector tmpVector = new Vector();
    private Vector targetRotation = new Vector();

    private Vector rotationDir = new Vector();

    private Vector desiredPosition = new Vector();
    private Vector velocity;
    private Vector forward;

    private PlanarShadowCaster shadowCaster;

    public PlayerTank() {
        velocity = new Vector();
        forward = new Vector();

        collisionHolder = attachComponent(CollisionHolder.class);
        rotationDir = rot0;

        shadowCaster = new PlanarShadowCaster();
        MeshRenderer.ShadowCaster = shadowCaster;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        desiredPosition.set(Position);
    }

    @Override
    public void loadResources() {
        MeshRenderer.Mesh = World.Runtime.ResourceManager.getMesh("mesh/t72_tank.mdl");
        MeshRenderer.Material = Material.createDiffuse("t72_primary", World.Runtime.ResourceManager.getTexture("textures/t72_diffuse.tex"));

        /*boundsMinV.set(MeshRenderer.Mesh.BoundingMin);
        boundsMaxV.set(MeshRenderer.Mesh.BoundingMax);
        boundsMaxV.Z *= 2;*/
    }

    // Returns true if tank was rotated enough to start moving
    private void chooseDirection(float x, float y) {
        if(x == -1.0f) {
            rotationDir = rot270;

            return;
        }

        if(x == 1.0f) {
            rotationDir = rot90;

            return;
        }

        if(y == -1.0f) {
            rotationDir = rot0;

            return;
        }

        if(y == 1.0f) {
            rotationDir = rot180;

            return;
        }
    }

    private void move(float x, float y) {
        chooseDirection(x, y);

        Mesh mesh = MeshRenderer.Mesh;

        // Calculate forward vector for desired rotation dir
        forward.calculateForward(rotationDir);

        collisionHolder.Min.set(forward.X - mesh.BoundingMax.X, forward.Y - mesh.BoundingMax.Y, forward.Z - mesh.BoundingMax.Z);
        collisionHolder.Max.set(forward.X + mesh.BoundingMax.X, forward.Y + mesh.BoundingMax.Y, forward.Z + mesh.BoundingMax.Z);

        boolean canMove = Rotation.compare(rotationDir, 5.0f);
        tmpVector.set(Position);

        velocity.X = 0;
        velocity.Z = 0;

        if((x == -1.0f || x == 1.0f) && canMove) {
            Position.X += x * ACCELERATION_FACTOR;
            velocity.X = x;
            canMove = false; // Single axis at time
        }

        if((y == -1.0f || y == 1.0f) && canMove) {
            Position.Z += y * ACCELERATION_FACTOR;
            velocity.Z = y;
        }

        // Check collision with walls
        if(collisionHolder.isIntersectingWithAnyone(CollisionHolder.TAG_STATIC) != null) {
            Position.set(tmpVector);
            desiredPosition.set(tmpVector);
        }
    }

    private void updateCamera() {
        final float EASE_SPEED = 0.04f;

        forward.Z = -10;
        tmpVector.set(Position);
        tmpVector.add(forward);
        tmpVector.Y = 20;

        targetRotation.X = 75 + (-velocity.Z * 5);
        targetRotation.Y = velocity.X * 15;

        World.Camera.Position.lerp(World.Camera.Position, tmpVector, EASE_SPEED);
        World.Camera.Rotation.lerp(World.Camera.Rotation, targetRotation, EASE_SPEED);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        shadowCaster.rebuildMatrix(MeshRenderer.Matrix, new Vector(0, 1, 1));

        float x = World.Runtime.Input.isKeyPressed(KeyCodes.KEY_LEFT) ? -1 : (World.Runtime.Input.isKeyPressed(KeyCodes.KEY_RIGHT) ? 1 : 0);
        float y = World.Runtime.Input.isKeyPressed(KeyCodes.KEY_UP) ? 1 : (World.Runtime.Input.isKeyPressed(KeyCodes.KEY_DOWN) ? -1 : 0);

        move(x, y);
        Rotation.lerp(Rotation, rotationDir, 0.1f);

        forward.calculateForward(Rotation);
        updateCamera();
    }
}
