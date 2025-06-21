package com.monobogdan.engine.world.components;

import com.monobogdan.engine.world.Component;

public class Animator extends Component {
    interface AnimationTarget {
        void onAnimate(Animator animator);
    }

    public static class Animation {
        public String Name;
        public int StartFrame, EndFrame;
        public float Speed;

        public Animation(String name, int startFrame, int endFrame, float speed) {
            StartFrame = startFrame;
            EndFrame = endFrame;
            Speed = speed;
            Name = name;
        }
    }

    private AnimationTarget target;

    public Animation Animation;
    public boolean IsPlaying;

    private float time;

    public Animator() {

    }

    public void setTarget(AnimationTarget target) {
        this.target = target;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
    }
}
