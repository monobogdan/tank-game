package com.monobogdan.game.tasks;

public class DestroyObjective implements MissionScript.Objective {
    private int destroyCount;
    private String description;
    private String name;

    public DestroyObjective(String desc, int destroyCount) {
        if(destroyCount < 1)
            throw new IllegalArgumentException("destroyCount can't be less than 1");

        if(desc == null)
            throw new NullPointerException("Objective description can't be null");

        this.destroyCount = destroyCount;
        this.description = desc;

        name = "Destruction";
    }

    @Override
    public boolean isCompleted() {
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void update() {

    }

    @Override
    public void draw() {

    }
}
