package com.monobogdan.game.objects;

import com.monobogdan.engine.BaseInput;
import com.monobogdan.engine.Input;
import com.monobogdan.engine.KeyCodes;
import com.monobogdan.engine.Material;
import com.monobogdan.engine.MaterialLoader;
import com.monobogdan.engine.Mesh;
import com.monobogdan.engine.math.MathUtils;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.world.StaticMesh;
import com.monobogdan.engine.world.components.CollisionHolder;

public class PlayerTank extends StaticMesh {
    public static final float MAX_SPEED = 1.0f;
    public static final float ACCELERATION_FACTOR = 8.0f;
    public static final float ROTATION_FACTOR = 7.5f;
    public static final float DECELERATION_FACTOR = 7.0f;

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

    private float inputX, inputY;
    private boolean inputFire;

    private float nextAttack;

    public PlayerTank() {
        super(false);

        collisionHolder = attachComponent(CollisionHolder.class);

        velocity = new Vector();
        forward = new Vector();

        rotationDir = rot0;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        desiredPosition.set(Position);
    }

    @Override
    public void loadResources() {
        MeshRenderer.Mesh = World.Runtime.ResourceManager.getMesh("mesh/t72_tank.mdl");
        //MeshRenderer.Material = Material.createDiffuse("t72_primary", World.Runtime.ResourceManager.getTexture("textures/t72_diffuse.tex"));
        MeshRenderer.Material = MaterialLoader.load(World.Runtime, "materials/tank_diffuse.mtl");

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

    public boolean canMove() {
        return Rotation.compare(rotationDir, 5.0f);
    }

    public boolean canRotate() {
        return Position.compare(desiredPosition, 0.5f);
    }

    private void move(float x, float y, float delta) {
        chooseDirection(x, y);

        Mesh mesh = MeshRenderer.Mesh;

        // Calculate forward vector for desired rotation dir
        forward.calculateForward(rotationDir);

        collisionHolder.Min.set(forward.X - mesh.BoundingMax.X, forward.Y - mesh.BoundingMax.Y, forward.Z - mesh.BoundingMax.Z);
        collisionHolder.Max.set(forward.X + mesh.BoundingMax.X, forward.Y + mesh.BoundingMax.Y, forward.Z + mesh.BoundingMax.Z);

        boolean canMove = canMove();
        tmpVector.set(Position);

        velocity.X = 0;
        velocity.Z = 0;

        if((x == -1.0f || x == 1.0f) && canMove) {
            Position.X += x * (ACCELERATION_FACTOR * delta);
            velocity.X = x;
            canMove = false; // Single axis at time
        }

        if((y == -1.0f || y == 1.0f) && canMove) {
            Position.Z += y * (ACCELERATION_FACTOR * delta);
            velocity.Z = y;
        }

        Rotation.lerp(Rotation, rotationDir, ROTATION_FACTOR * delta);

        // Check collision with walls
        if(collisionHolder.isIntersectingWithAnyone(CollisionHolder.TAG_STATIC) != null) {
            //Position.set(tmpVector);
            Position.set(tmpVector);
        }

       // Position.lerp(Position, desiredPosition, DECELERATION_FACTOR * delta);
    }

    private void updateCamera(float delta) {
        final float EASE_SPEED = 1.5f;

        forward.Z = -10;
        tmpVector.set(Position);
        tmpVector.add(forward);
        tmpVector.Y = 20;

        targetRotation.X = 75 + (-velocity.Z * 10);
        targetRotation.Y = velocity.X * 5;

        World.Camera.Position.lerp(World.Camera.Position, tmpVector, EASE_SPEED * delta);
        World.Camera.Rotation.lerp(World.Camera.Rotation, targetRotation, EASE_SPEED * delta);
    }

    private void updateInput() {
        //inputX = World.Runtime.Input.isKeyPressed(KeyCodes.KEY_A) ? -1 : (World.Runtime.Input.isKeyPressed(KeyCodes.KEY_D) ? 1 : 0);
        //inputY = World.Runtime.Input.isKeyPressed(KeyCodes.KEY_W) ? 1 : (World.Runtime.Input.isKeyPressed(KeyCodes.KEY_S) ? -1 : 0);

        inputX = World.Runtime.Input.getAxis(Input.AXIS_HORIZONTAL);
        inputY = -World.Runtime.Input.getAxis(Input.AXIS_VERTICAL);

        inputFire = World.Runtime.Input.getGamePadState(BaseInput.GamePad.KEY_A) == BaseInput.STATE_PRESSED;

        inputX += World.Runtime.Game.TouchGamepad.HorizontalInput;
        inputY += World.Runtime.Game.TouchGamepad.VerticalInput;

        inputX = MathUtils.clamp(inputX, -1, 1);
        inputY = MathUtils.clamp(inputY, -1, 1);
    }

    private void primaryAttack() {
        final float FIRE_COOLDOWN = 1.0f;

        if(World.Runtime.Time.TimeSinceGameStart > nextAttack && canMove()) {
            Bullet bullet = new Bullet();
            bullet.Position.set(forward);
            bullet.Position.multiply(3);
            bullet.Position.add(Position);
            bullet.Position.add(0, 0.5f, 0);
            bullet.Rotation.set(Rotation);

            World.spawn(bullet);

            nextAttack = World.Runtime.Time.TimeSinceGameStart + FIRE_COOLDOWN;
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        float dt = World.Runtime.Time.DeltaTime;

        forward.calculateForward(Rotation);

        updateInput();
        move(inputX, inputY, dt);

        if(inputFire)
            primaryAttack();

        updateCamera(dt);
    }
}
