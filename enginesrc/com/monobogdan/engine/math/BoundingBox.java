package com.monobogdan.engine.math;

public class BoundingBox {

    public static boolean test(float minX, float minY, float minZ,
                               float maxX, float maxY, float maxZ,
                               float minX2, float minY2, float minZ2,
                               float maxX2, float maxY2, float maxZ2) {
        return (minX < maxX2 && minZ < maxZ2 && minX2 < maxX && minZ2 < maxZ);

        //(X < box.X + box.X2 && Y < box.Y + box.Y2 && Z < box.Z + box.Z2 && box.X < X + X2 && box.Y < Y + Y2 && box.Z < Z + Z2);
    }
}
