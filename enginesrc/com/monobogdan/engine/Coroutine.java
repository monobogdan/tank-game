package com.monobogdan.engine;

import java.util.Stack;
import java.util.Vector;

public final class Coroutine {
    public static abstract class Runnable {
        boolean firstRun;

        void prepare() { }
        protected abstract boolean run();
    }

    public interface Updater<T> {
        void run(T arg);
    }

    private Runtime runtime;
    private Vector<Runnable> coroutineStack;

    public Coroutine(Runtime runtime) {
        this.runtime = runtime;

        coroutineStack = new Vector<Runnable>();
    }

    public Coroutine after(Runnable runnable) {
        if(runnable == null)
            throw new RuntimeException("Runnable can't be null for run");

        coroutineStack.add(runnable);

        return this;
    }

    public boolean isBusy() {
        return !coroutineStack.isEmpty();
    }

    public void update() {
        if(!coroutineStack.isEmpty()) {
            Runnable runnable = coroutineStack.get(0);

            if(!runnable.firstRun) {
                runnable.prepare();
                runnable.firstRun = true;
            }

            if(!runnable.run())
                coroutineStack.remove(0);
        }
    }

    public static Runnable waitForMillis(final Runtime runtime, final float time) {
        return new Runnable() {
            private float elapseTime;

            @Override
            public void prepare() {
                elapseTime = time + runtime.Time.TimeSinceGameStart;
            }

            @Override
            public boolean run() {
                return runtime.Time.TimeSinceGameStart < elapseTime;
            }
        };
    }

    public static Runnable fadeOverTime(final Runtime runtime, final float time, final Updater<Float> updater) {
        return new Runnable() {
            private float timeStart;

            @Override
            public void prepare() {
                timeStart = runtime.Time.TimeSinceGameStart;
            }

            @Override
            public boolean run() {
                float progress = (runtime.Time.TimeSinceGameStart - timeStart) / time;

                if(progress < 1.0f)
                    updater.run(progress);

                return progress < 1.0f;
            }
        };
    }
}
