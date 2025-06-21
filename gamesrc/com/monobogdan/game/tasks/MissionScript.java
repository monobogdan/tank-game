package com.monobogdan.game.tasks;

import com.monobogdan.game.Game;

public abstract class MissionScript {
    public static final int MISSION_STATE_IN_PROCESS = 0;
    public static final int MISSION_STATE_COMPLETE = 1;
    public static final int MISSION_STATE_FAILED = 2;

    interface Objective {
        boolean isCompleted();
        String getName();
        String getDescription();

        void update();
        void draw();
    }

    private Game game;
    private Objective objective;
    private int state;

    public MissionScript(Game game) {
        this.game = game;
    }

    public void setObjective(Objective task) {
        this.objective = task;
    }

    protected final void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public final void update() {
        if(objective == null)
            throw new NullPointerException("Mission script didn't set objective");

        objective.update();
    }

    public final void draw() {
        objective.draw();
    }

    public final void drawHUD() {

    }
}
