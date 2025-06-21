package com.monobogdan.game.objects;

import com.monobogdan.engine.world.GameObject;

public class Point extends GameObject {
    public static final String POINT_PLAYER_START = "PlayerStart";
    public static final String POINT_ENEMY_SPAWN = "EnemySpawn";
    public static final String POINT_BONUS_SPAWN = "BonusSpawn";

    public String PointType;

    public Point(String type) {
        PointType = type;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onUpdate() {

    }

    @Override
    public void onDestroy() {

    }
}
