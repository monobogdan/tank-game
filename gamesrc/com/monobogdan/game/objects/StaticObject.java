package com.monobogdan.game.objects;

import com.monobogdan.engine.Material;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.world.StaticMesh;
import com.monobogdan.engine.world.components.CollisionHolder;

public class StaticObject extends StaticMesh {
    private String meshName;
    private String meshTexture;

    private CollisionHolder collisionHolder;

    public boolean HasCollision;

    public StaticObject(boolean hasCollision, String meshName, String textureName) {
        HasCollision = hasCollision;

        this.meshName = meshName;
        this.meshTexture = textureName;

        if(hasCollision) {
            collisionHolder = attachComponent(CollisionHolder.class);
            collisionHolder.Tag = CollisionHolder.TAG_STATIC;
        }
    }

    @Override
    public void loadResources() {
        MeshRenderer.Mesh = World.Runtime.ResourceManager.getMesh("mesh/" + meshName);
        MeshRenderer.Material = Material.createDiffuse(meshTexture, World.Runtime.ResourceManager.getTexture("textures/" + meshTexture));

        if(collisionHolder != null) {
            collisionHolder.Min.set(MeshRenderer.Mesh.BoundingMin);
            collisionHolder.Max.set(MeshRenderer.Mesh.BoundingMax);
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
    }
}
