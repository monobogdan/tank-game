package com.monobogdan.engine.world.components;

import com.monobogdan.engine.Camera;
import com.monobogdan.engine.Graphics;
import com.monobogdan.engine.math.BoundingBox;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.world.Component;
import com.monobogdan.engine.world.GameObject;

public class CollisionHolder extends Component {
    public static final int TAG_NONE = -1;
    public static final int TAG_STATIC = 0;
    public static final int TAG_DYNAMIC = 1;
    // Other tags might be defined in subclasses

    public Vector Min = new Vector();
    public Vector Max = new Vector();

    public int Tag;

    public CollisionHolder() {

    }

    @Override
    public void onDraw(Graphics graphics, Camera camera, int renderPassFlags) {
        //graphics.drawBoundingBox(camera, Min, Max, Parent.Position.X, Parent.Position.Y, Parent.Position.Z);
    }

    public GameObject isIntersectingWithAnyone(int tag) {
        for(GameObject obj : Parent.World.GameObjects) {
            if(obj != Parent) {
                CollisionHolder holder = obj.getComponent(CollisionHolder.class);

                // TODO: Broadphase
                if(holder != null && (tag == -1 || tag == holder.Tag)) {
                    if(BoundingBox.test(Parent.Position.X + Min.X, Parent.Position.Y + Min.Y, Parent.Position.Z +  Min.Z,
                            Parent.Position.X + Max.X, Parent.Position.Y + Max.Y, Parent.Position.Z + Max.Z,
                            obj.Position.X + holder.Min.X, obj.Position.Y + holder.Min.Y, obj.Position.Z + holder.Min.Z,
                            obj.Position.X + holder.Max.X, obj.Position.Y + holder.Max.Y, obj.Position.Z + holder.Max.Z))
                        return obj;
                }
            }
        }

        return null;
    }
}
