package com.monobogdan.game.tasks;

import com.monobogdan.game.Game;

public class GenericTask extends MissionScript {

    public GenericTask(Game game) {
        super(game);

        setObjective(new DestroyObjective("Destroy 30 tanks", 30));
    }
}
