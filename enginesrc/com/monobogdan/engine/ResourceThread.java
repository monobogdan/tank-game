package com.monobogdan.engine;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

// Helper class for organizing multi-threaded data loading
public class ResourceThread {
    public interface LoadingWorker {
        void onBeforeLoad(AsyncResult res);
        void onLoad(AsyncResult res);
    }
    public static class AsyncResult {
        private Runtime runtime;
        private String name;

        private Future future;
        private volatile int progress;
        private volatile String progressStage;

        AsyncResult(Runtime runtime, String threadName) {
            this.runtime = runtime;
            this.name = threadName;
        }

        public String getThreadName() {
            return name;
        }

        public Future getFuture() {
            return future;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public void setProgressStage(String progressStage) {
            this.progressStage = progressStage;
        }

        public boolean isDone() {
            return future.isDone();
        }

        public boolean isSuccessful() {
            if(!future.isDone())
                throw new IllegalStateException("future.isDone was false");

            Throwable innerException = null;

            try {
                future.get();
            } catch (ExecutionException e) {
                innerException = e;
            } catch (InterruptedException e) {
                innerException = e;
            }

            if(innerException != null)
                throw new RuntimeException("Worker thread thrown an exception", innerException);

            return true;
        }

        public String getLoadingStage() {
            return progressStage;
        }

        public int getProgress() {
            return progress;
        }
    }

    private static ExecutorService execService = Executors.newFixedThreadPool(1);

    public static AsyncResult start(final Runtime runtime, final LoadingWorker worker, String name) {
        if(name == null)
            throw new NullPointerException("Attempt to start unnamed loading thread");

        if(worker == null)
            throw new NullPointerException("Worker can't be null for thread " + name);

        final AsyncResult res = new AsyncResult(runtime, name);
        worker.onBeforeLoad(res);

        res.future = execService.submit(new Runnable() {
            @Override
            public void run() {
                runtime.Platform.log("Started loading thread %s", res.getThreadName());

                worker.onLoad(res);
                runtime.Platform.log("Loading thread %s successfully completed job", res.getThreadName());
            }
        });

        return res;
    }

    static void staticFinalize() {
        execService.shutdown();
    }
}
