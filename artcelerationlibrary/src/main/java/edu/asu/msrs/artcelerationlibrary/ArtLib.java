package edu.asu.msrs.artcelerationlibrary;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import edu.asu.msrs.artcelerationlibrary.artcelerationService.ArtTransformService;

/**
 * Created by rlikamwa on 10/2/2016.
 */

public class ArtLib {

    private TransformHandler artTransformListener;
    private Activity mActivity;
    private Messenger mServiceMessenger;
    private boolean mBound = false;

    final Messenger mArtLibMessenger = new Messenger(new ArtLibHandler());

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


    public ArtLib(Activity activity) {
        mActivity = activity;
        init();
    }

    private void init() {

        Intent intent = new Intent(mActivity, ArtTransformService.class);
        mActivity.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }

    public String[] getTransformsArray() {
        String[] transforms = {"Gaussian Blur", "Neon edges", "Color Filter"};
        return transforms;
    }

    public TransformTest[] getTestsArray() {
        TransformTest[] transforms = new TransformTest[3];
        transforms[0] = new TransformTest(0, new int[]{1, 2, 3}, new float[]{0.1f, 0.2f, 0.3f});
        transforms[1] = new TransformTest(1, new int[]{11, 22, 33}, new float[]{0.3f, 0.2f, 0.3f});
        transforms[2] = new TransformTest(2, new int[]{51, 42, 33}, new float[]{0.5f, 0.6f, 0.3f});

        return transforms;
    }

    public void registerHandler(TransformHandler artlistener) {
        this.artTransformListener = artlistener;
    }

    public boolean requestTransform(Bitmap img, int index, int[] intArgs, float[] floatArgs) {

        Bundle dataBundle = new Bundle();

        try {
            int width = img.getWidth();
            int height = img.getHeight();
            int bytes = img.getByteCount();

            // Allocate buffer whose size based on the src_img
            ByteBuffer buffer = ByteBuffer.allocate(bytes);
            img.copyPixelsToBuffer(buffer);
            byte[] byteArray = buffer.array();
            MemoryFile memoryFile = new MemoryFile("someone", byteArray.length);
            memoryFile.writeBytes(byteArray, 0, 0, byteArray.length);
            ParcelFileDescriptor pfd = MemoryFileUtil.getParcelFileDescriptor(memoryFile);
            memoryFile.close();

            // put data into dataBundle
            dataBundle.putIntArray("intArgs", intArgs);
            dataBundle.putFloatArray("floatArgs", floatArgs);
            dataBundle.putParcelable("pfd", pfd);
            dataBundle.putInt("width", width);
            dataBundle.putInt("height", height);
            dataBundle.putInt("index", index);

            // create message to be sent to ArtTransformService
            Message message = Message.obtain(null, index, width, height);
            message.setData(dataBundle);
            // tell the ArtTransformHandler reply to ArtLibHandler
            message.replyTo = mArtLibMessenger;
            mServiceMessenger.send(message);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

//        ArtTransformTaskCallable callable = new ArtTransformTaskCallable();
//        callable.setArtTransformThreadPool(artTransformThreadPool);
//        artTransformThreadPool.addArtTransformTask(callable);

        return true;
    }

    // ArtLib Handler: receive message from service and tell UI thread to update img
    public class ArtLibHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            Bundle dataBundle = msg.getData();
            ParcelFileDescriptor pfd = (ParcelFileDescriptor) dataBundle.get("pfd");

            InputStream inputStream = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
            byte[] byteArray = new byte[0];
            try {
                byteArray = IOUtils.toByteArray(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Bitmap img = Bitmap.createBitmap(msg.arg1, msg.arg2, Bitmap.Config.valueOf("ARGB_8888"));
                ByteBuffer buffer = ByteBuffer.wrap(byteArray);
                img.copyPixelsFromBuffer(buffer);

                if (artTransformListener != null) {
                    artTransformListener.onTransformProcessed(img);
                    Log.d("AsyncTask", "Transform Finished!");
                }
            }

        }
    }

}
