package edu.asu.msrs.artcelerationlibrary.artcelerationService;


import android.os.Looper;

public class ArtTransformThread extends Thread{
    private static final String TAG = ArtTransformThread.class.getSimpleName();
    public ArtTransformHandler mArtTransformHandler;

    @Override
    public void run() {
        Looper.prepare();
        mArtTransformHandler = new ArtTransformHandler();
        Looper.loop();
    }
}
