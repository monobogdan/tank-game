package com.monobogdan.engine.world.components;

import com.monobogdan.engine.Camera;
import com.monobogdan.engine.math.Vector;
import com.monobogdan.engine.Graphics;
import com.monobogdan.engine.Texture2D;
import com.monobogdan.engine.world.Component;

public class ParticleSystem extends Component {
    public class Particle {
        public Vector Position = new Vector();
        public Texture2D Texture;
        public Vector Color = new Vector();
        public float Opacity;
        public float Size;
        public float TimeSinceCreated;
        public float LifeTime;

        public Particle(Vector pos, Texture2D tex, Vector color, float opacity) {
            Position = pos;
            Texture = tex;
            Color = color;
            Opacity = opacity;
        }
    }

    public interface SimulationFactor {
        void onUpdate(ParticleSystem particleSystem, Particle particle);
    }

    private java.util.Vector<Particle> particles = new java.util.Vector<Particle>();
    private java.util.Vector<Particle> removalVector = new java.util.Vector<Particle>();
    private java.util.Vector<SimulationFactor> factors = new java.util.Vector<SimulationFactor>();

    public ParticleSystem() {
        setDesiredParticleCount(128);
    }

    public void addSimulationFactor(SimulationFactor factor) {
        if(factors.contains(factor))
            throw new RuntimeException("ParticleSystem on object " + Parent + " already contains SimulationFactor " + factor.getClass().getSimpleName());

        factors.add(factor);
    }

    public void setDesiredParticleCount(int count) {
        particles.ensureCapacity(count);
        removalVector.ensureCapacity(count);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        for(int i = 0; i < particles.size(); i++) {
            Particle p = particles.get(i);

            for(int j = 0; j < factors.size(); j++)
                factors.get(j).onUpdate(this, p);
        }

        for(int i = 0; i < removalVector.size(); i++)
            particles.remove(removalVector.get(i));

        removalVector.clear();
    }

    @Override
    public void onDraw(Graphics graphics, Camera camera, int renderPassFlags) {
        super.onDraw(graphics, camera, renderPassFlags);
    }
}
