package com.monobogdan.engine;

public class Time {
    private static final float NANOSECONDS_TO_FLOAT = 1000000000;

    public float Scale;
    public float TimeSinceGameStart;
    public float DeltaTime;
    public int FixedUpdateInterval;

    private long globalNanoSecondTimer;
    private long nanoSeconds;

    public Time() {
        Scale = 1.0f;

        resetGlobalTimer();
    }

    // Ensure that no entities depends from this value
    public void resetGlobalTimer() {
        globalNanoSecondTimer = System.nanoTime();
    }

    void update() {
        nanoSeconds = System.nanoTime();
    }

    void endUpdate() {
        DeltaTime = (float)(System.nanoTime() - nanoSeconds) / NANOSECONDS_TO_FLOAT;
        TimeSinceGameStart = (float)(System.nanoTime() - globalNanoSecondTimer) / NANOSECONDS_TO_FLOAT;
    }
}
