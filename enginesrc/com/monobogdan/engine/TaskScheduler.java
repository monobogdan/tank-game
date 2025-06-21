package com.monobogdan.engine;

import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskScheduler {
    class Task {
        public Runnable Runnable;
        public volatile boolean isCompleted;

        public Task(Runnable runnable) {
            Runnable = runnable;
        }
    }

    private Runtime runtime;
    private Stack<Task> tasks;

    public TaskScheduler(Runtime runtime) {
        this.runtime = runtime;

        tasks = new Stack<Task>();
        tasks.ensureCapacity(32);
    }

    public Task runOnMainThread(Runnable runnable) {
        if(runnable == null)
            throw new IllegalArgumentException("Runnable can't be null");

        return tasks.push(new Task(runnable));
    }

    // Needed for resource upload in thread-safe manner
    public void runOnMainThreadIfNeeded(Runnable runnable) {
        if(Thread.currentThread() != runtime.MainThread)
            runOnMainThread(runnable);
        else
            runnable.run();
    }

    void update() {
        while(!tasks.isEmpty()) {
            tasks.pop().Runnable.run();
        }
    }
}
