package edu.asu.msrs.artcelerationlibrary.artcelerationService;


import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

public class ArtTransformTaskCallable implements Callable{

    private static final String TAG = "ArtTransformCallable";
    private WeakReference<ArtTransformThreadPool> mArtTransformThreadPoolWeakReference;


    @Override
    public Object call() throws Exception {
        try {
            // check if thread is interrupted before lengthy operation
            if (Thread.interrupted()) throw new InterruptedException();

            // In real world project, you might do some blocking IO operation
            // In this example, I just let the thread sleep for 3 second
//            Thread.sleep(5000);

            long endTime = System.currentTimeMillis() + 5*1000;
            while (System.currentTimeMillis() < endTime) {
                try {
                    Log.d(TAG, "do the ArtTransform!");
                    Thread.sleep(10*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "finished!");
            Log.d(TAG, String.valueOf(Thread.currentThread().getName()));


            //Log.d("ArtTransformCallable", "do the ArtTransform!");

            // After work is finished, send a message to CustomThreadPoolManager
            Message message = Util.createMessage(Util.MESSAGE_ID, "Thread " +
                    String.valueOf(Thread.currentThread().getId()) + " " +
                    String.valueOf(Thread.currentThread().getName()) + " completed");

            if(mArtTransformThreadPoolWeakReference != null
                    && mArtTransformThreadPoolWeakReference.get() != null) {

                Log.d(TAG, mArtTransformThreadPoolWeakReference.toString());
                mArtTransformThreadPoolWeakReference.get().sendMessageToUiThread(message);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setArtTransformThreadPool (ArtTransformThreadPool artTransformThreadPool) {
        this.mArtTransformThreadPoolWeakReference = new WeakReference<ArtTransformThreadPool>(artTransformThreadPool);
    }
}
