package edu.asu.msrs.artcelerationlibrary.artcelerationService;


import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ArtTransformThreadPool {

    private static ArtTransformThreadPool sArtTransformThreadPoolIns = null;

    private final BlockingQueue<Runnable> mArtTransformRequestQueue;
    private List<Future> mDoingArtTransform;
    private final ExecutorService mArtTransformThreadPool;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;
    private static final int KEEP_ALIVE_TIME = 1;
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    static {
        sArtTransformThreadPoolIns = new ArtTransformThreadPool();
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    }

    private ArtTransformThreadPool() {
        mArtTransformRequestQueue = new LinkedBlockingQueue<Runnable>();
        mDoingArtTransform = new ArrayList<>();
        mArtTransformThreadPool = Executors.newFixedThreadPool(NUMBER_OF_CORES, new BackgroundThreadFactory());
    }

    public static ArtTransformThreadPool getInstance() {
        return sArtTransformThreadPoolIns;
    }

    // Add a task
    public void addArtTransformTask(Callable callable) {
        Future future = mArtTransformThreadPool.submit(callable);
        mDoingArtTransform.add(future);
    }

    public void cancelAll() {
        synchronized (this) {
            mArtTransformRequestQueue.clear();
            for (Future task : mDoingArtTransform) {
                if (!task.isDone()) {
                    task.cancel(true);
                }
            }
            mDoingArtTransform.clear();
        }
    }

    private static class BackgroundThreadFactory implements ThreadFactory {
        private static int sTag = 1;

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("CustomThread" + sTag);
            thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);

            // A exception handler is created to log the exception from threads
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    Log.e("BackgroundThread", thread.getName() + " encountered an error: " + ex.getMessage());
                }
            });
            return thread;
        }
    }

}
