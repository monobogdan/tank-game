package com.monobogdan.game.world;

import com.monobogdan.engine.math.Vector;

public class Map {
    public static int TILE_NONE = 0;
    public static int TILE_WOOD = 1;
    public static int TILE_BRICK = 2;
    public static int TILE_HARDBRICK = 3;

    public String[] Textures;
    public int[] Tiles;
    public Vector[] SpawnPoints;
    public Vector PlayerSpawn;

    public Map(String[] textures) {
        Textures = textures;
    }

    public void setGrid(int[] tiles) {
        Tiles = tiles;
    }

    public void setSpawnPoints(Vector[] spawnPoints) {
        SpawnPoints = spawnPoints;
    }

    public void setPlayerSpawn(Vector playerSpawn) {
        PlayerSpawn = playerSpawn;
    }
}
