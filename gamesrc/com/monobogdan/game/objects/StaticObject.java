package com.monobogdan.game.objects;

import com.monobogdan.engine.Material;
import com.monobogdan.engine.MaterialLoader;
import com.monobogdan.engine.world.StaticMesh;
import com.monobogdan.engine.world.components.CollisionHolder;

public class StaticObject extends StaticMesh {
    private String meshName;
    private String meshTexture;

    private CollisionHolder collisionHolder;

    public boolean HasCollision;

    public StaticObject(boolean hasCollision, boolean canBeOccluded, String meshName, String materialName) {
        super(true);

        HasCollision = hasCollision;

        this.meshName = meshName;
        this.meshTexture = materialName;

        MeshRenderer.CanBeOccluded = canBeOccluded;

        if(hasCollision) {
            collisionHolder = attachComponent(CollisionHolder.class);
            collisionHolder.Tag = CollisionHolder.TAG_STATIC;
        }

        Active = false; // Static object doesn't need to be active and therefore consume CPU cycles for update
    }

    @Override
    public void loadResources() {
        MeshRenderer.Mesh = World.Runtime.ResourceManager.getMesh("mesh/" + meshName);
        MeshRenderer.Material = World.Runtime.ResourceManager.getMaterial("materials/" + meshTexture);

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
