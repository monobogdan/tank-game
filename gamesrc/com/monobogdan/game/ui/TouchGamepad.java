package com.monobogdan.game.ui;

import com.monobogdan.engine.Texture2D;
import com.monobogdan.game.Game;

public class TouchGamepad {
    private static float UI_BASE_SIZE = 0.1f;

    private Game game;
    private Texture2D arrowLeft, arrowRight, arrowUp, arrowDown;

    public float Scale;
    public float HorizontalInput;
    public float VerticalInput;

    public TouchGamepad(Game game) {
        this.game = game;

        Scale = 1.7f;

        arrowUp = game.Runtime.ResourceManager.getTexture("textures/ui/arrow.tex");
        arrowRight = game.Runtime.ResourceManager.getTexture("textures/ui/arrow90.tex");
        arrowDown = game.Runtime.ResourceManager.getTexture("textures/ui/arrow180.tex");
        arrowLeft = game.Runtime.ResourceManager.getTexture("textures/ui/arrow270.tex");
    }

    public void drawUI() {
        VerticalInput = 0;
        HorizontalInput = 0;

        float scaled = UI_BASE_SIZE * Scale;
        float baseY = 1.0f - (scaled * 3); // 0.7f is base coefficient for 1.0f scaling

        if(game.Runtime.UI.imageButton(arrowUp, scaled, baseY, scaled, scaled, true))
            VerticalInput = 1;

        if(game.Runtime.UI.imageButton(arrowDown, scaled, baseY + (scaled * 2), scaled, scaled, true))
            VerticalInput = -1;

        if(game.Runtime.UI.imageButton(arrowLeft, 0.0f, baseY + scaled, scaled, scaled, true))
            HorizontalInput = -1;

        if(game.Runtime.UI.imageButton(arrowRight, scaled * 2, baseY + scaled, scaled, scaled, true))
            HorizontalInput = 1;
    }
}
