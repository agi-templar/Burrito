package edu.asu.msrs.artcelerationlibrary;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import edu.asu.msrs.artcelerationlibrary.artcelerationService.ArtTransformService;

/**
 * Created by rlikamwa on 10/2/2016.
 */

public class ArtLib {
    private TransformHandler artlistener;
    private Activity mActivity;
    private Messenger mServiceMessenger;
    private boolean mBound = false;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBound = true;
            mServiceMessenger = new Messenger(service);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            mServiceMessenger = null;
        }
    };


    public ArtLib(Activity activity){
        mActivity = activity;
        init();
    }

    private void init() {

        Intent intent = new Intent(mActivity, ArtTransformService.class);
        mActivity.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public String[] getTransformsArray(){
        String[] transforms = {"Gaussian Blur", "Neon edges", "Color Filter"};
        return transforms;
    }

    public TransformTest[] getTestsArray(){
        TransformTest[] transforms = new TransformTest[3];
        transforms[0]=new TransformTest(0, new int[]{1,2,3}, new float[]{0.1f, 0.2f, 0.3f});
        transforms[1]=new TransformTest(1, new int[]{11,22,33}, new float[]{0.3f, 0.2f, 0.3f});
        transforms[2]=new TransformTest(2, new int[]{51,42,33}, new float[]{0.5f, 0.6f, 0.3f});

        return transforms;
    }

    public void registerHandler(TransformHandler artlistener){
        this.artlistener=artlistener;
    }

    public boolean requestTransform(Bitmap img, int index, int[] intArgs, float[] floatArgs) {

        Message message = Message.obtain(null,index);
        try {
            mServiceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        return true;
    }

}
