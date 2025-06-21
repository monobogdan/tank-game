package com.monobogdan.engine.world.components;

import com.monobogdan.engine.Camera;
import com.monobogdan.engine.Graphics;
import com.monobogdan.engine.Texture2D;
import com.monobogdan.engine.world.Component;

public class SpriteSheetRenderer extends Component implements Animator.AnimationTarget {
    public static class AtlasFragment {
        public int X, Y, Width, Height;
    }

    public Texture2D Texture;
    public AtlasFragment[] Frames;

    public SpriteSheetRenderer() {

    }

    public void createAtlasFromGrid(int gridWidth, int gridHeight) {
        if(Texture == null)
            throw new RuntimeException("Can't create atlas grid without loaded texture");

        Frames = new AtlasFragment[gridWidth * gridHeight];

        int frameWidth = Texture.Width / gridWidth;
        int frameHeight = Texture.Height / gridHeight;

        for(int i = 0; i < gridWidth * gridHeight; i++) {
            int currY = i % gridWidth;
            int currX = i - ((i % gridWidth) * gridWidth);

            Frames[i] = new AtlasFragment();
            Frames[i].X = currX;
            Frames[i].Y = currY;
            Frames[i].Width = frameWidth;
            Frames[i].Height = frameHeight;
        }
    }

    @Override
    public void onAnimate(Animator animator) {

    }

    @Override
    public void onUpdate() {
        super.onUpdate();
    }

    @Override
    public void onDraw(Graphics graphics, Camera camera, int renderPassFlags) {
        super.onDraw(graphics, camera, renderPassFlags);

        if(Texture != null && Frames != null) {

        }
    }
}
