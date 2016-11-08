package edu.asu.msrs.artcelerationlibrary.artcelerationService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.util.Log;


public class ArtTransformService extends Service{

    private static final String TAG = ArtTransformService.class.getSimpleName();

    public Messenger mMessenger = new Messenger(new ArtTransformHandler());
    private ArtTransformHandler mArtTransformHandler;
    public ArtTransformService() {
    }


    @Override
    public void onCreate() {

        // Put service into a separate Thread named "ArtTransformThread"
        ArtTransformThread thread = new ArtTransformThread();
        thread.setName("ArtTransformThread");
        thread.start();

        // Make sure Handler is available
        while (thread.mArtTransformHandler == null) {

        }
        mArtTransformHandler = thread.mArtTransformHandler;
        mArtTransformHandler.setService(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Message message = Message.obtain();
        message.arg1 = startId;
        mArtTransformHandler.sendMessage(message);
        return Service.START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        //release client service
    }
}
