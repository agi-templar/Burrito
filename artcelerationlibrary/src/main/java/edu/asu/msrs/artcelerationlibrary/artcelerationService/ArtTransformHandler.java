package edu.asu.msrs.artcelerationlibrary.artcelerationService;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import edu.asu.msrs.artcelerationlibrary.MemoryFileUtil;

public class ArtTransformHandler extends Handler {
    private ArtTransformService mService;
    static ArrayList<Messenger> mClients = new ArrayList<>();
    static Messenger targetMessenger;
    List<ArtTransformAsyncTask> mArtTransformAsyncTasks;


    @Override
    public void handleMessage(Message msg) {

        targetMessenger = msg.replyTo;
        mArtTransformAsyncTasks = new ArrayList<>();

        switch (msg.what){
            case 0:
                Log.d("doTransform", "Gaussian_Blur");

                try {
                    new ArtTransformAsyncTask().executeOnExecutor(Executors.newCachedThreadPool(), loadImage(msg));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Log.d("AsyncTask", "Gaussian_Blur Finished");
                }

                break;
            case 1:
                Log.d("doTransform", "Neon_Edges");

                try {
                    new ArtTransformAsyncTask().executeOnExecutor(Executors.newCachedThreadPool(), loadImage(msg));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Log.d("AsyncTask", "Neon_Edges Finished");
                }


                break;
            case 2:
                Log.d("doTransform", "Color_Filter");

                try {
                    new ArtTransformAsyncTask().executeOnExecutor(Executors.newCachedThreadPool(), loadImage(msg));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Log.d("AsyncTask", "Color_Filter Finished");
                }

                break;
            default:
                break;
        }

    }

    public class ArtTransformAsyncTask extends AsyncTask <Bitmap, Void, Void> {

        private Bitmap rawBitmap;

        @Override
        protected void onPreExecute() {
            mArtTransformAsyncTasks.add(this);
        }

        @Override
        protected Void doInBackground(Bitmap... params) {

            rawBitmap = changeLight(params[0]);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            mArtTransformAsyncTasks.remove(this);
            if(mArtTransformAsyncTasks.size() == 0){
                Log.d("AsyncTask", "All Tasks Finished");
            }
            imageProcessed(rawBitmap);

        }

    }


    private Bitmap loadImage(Message msg) throws IOException {
        Bundle dataBundle = msg.getData();
        ParcelFileDescriptor pfd = (ParcelFileDescriptor) dataBundle.get("pfd");
        InputStream inputStream = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
        //Convert the inputStream to bitmap
        byte[] byteArray =  IOUtils.toByteArray(inputStream);
        //The configuration is ARGB_8888, if the configuration changed in the application, here should be changed
        // a better way is to pass the parameter through the message.
        Bitmap.Config configBmp = Bitmap.Config.valueOf("ARGB_8888");
        Bitmap rawBitmap = Bitmap.createBitmap(msg.arg1, msg.arg2, configBmp);
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        rawBitmap.copyPixelsFromBuffer(buffer);
        return rawBitmap;
    }


    private Bitmap changeSaturation(Bitmap img) {
        ColorMatrix colorMatrixSaturation = new ColorMatrix();
        ColorMatrix allColorMatrix = new ColorMatrix();

        colorMatrixSaturation.reset();
        colorMatrixSaturation.setSaturation(0.30f);

        allColorMatrix.reset();
        allColorMatrix.postConcat(colorMatrixSaturation);

        Bitmap newBitmap = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        paint.setColorFilter(new ColorMatrixColorFilter(allColorMatrix));
        canvas.drawBitmap(img, 0, 0, paint);
        return newBitmap;
    }

    private Bitmap changeHSL(Bitmap img) {
        ColorMatrix colorMatrixchangeHSL = new ColorMatrix();
        ColorMatrix allColorMatrix = new ColorMatrix();

        colorMatrixchangeHSL.reset();
        colorMatrixchangeHSL.setRotate(0, 20);
        colorMatrixchangeHSL.setRotate(1, 30);
        colorMatrixchangeHSL.setRotate(2, 10);

        allColorMatrix.reset();
        allColorMatrix.postConcat(colorMatrixchangeHSL);

        Bitmap newBitmap = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        paint.setColorFilter(new ColorMatrixColorFilter(allColorMatrix));
        canvas.drawBitmap(img, 0, 0, paint);
        return newBitmap;
    }

    private Bitmap changeLight(Bitmap img) {
        ColorMatrix colorMatrixchangeLight = new ColorMatrix();
        ColorMatrix allColorMatrix = new ColorMatrix();

        colorMatrixchangeLight.reset();
        colorMatrixchangeLight.setScale(1.5f, 1.5f, 1.5f, 1);

        allColorMatrix.reset();
        allColorMatrix.postConcat(colorMatrixchangeLight);

        Bitmap newBitmap = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        paint.setColorFilter(new ColorMatrixColorFilter(allColorMatrix));
        canvas.drawBitmap(img, 0, 0, paint);
        return newBitmap;
    }

    private void imageProcessed(Bitmap img){
        int width = img.getWidth();
        int height = img.getHeight();
        int what = 0;
        Message msg = Message.obtain(null, what,width,height);
        msg.replyTo = targetMessenger;

        //Message msg = Message.obtain(null, what);
        Bundle dataBundle = new Bundle();
        mClients.add(msg.replyTo);
        if (msg.replyTo == null) {
            Log.d("mclient is ", "null");
        }
        try {
            int bytes = img.getByteCount();
            ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
            img.copyPixelsToBuffer(buffer); //Move the byte data to the buffer
            byte[] byteArray = buffer.array();

            /*ByteArrayOutputStream stream = new ByteArrayOutputStream();
            img.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();*/
            //Secondly, put the stream into the memory file.
            MemoryFile memoryFile = new MemoryFile("someone", byteArray.length);
            memoryFile.writeBytes(byteArray, 0, 0, byteArray.length);
            ParcelFileDescriptor pfd = MemoryFileUtil.getParcelFileDescriptor(memoryFile);
            memoryFile.close();
            dataBundle.putParcelable("pfd", pfd);

            msg.setData(dataBundle);
            mClients.get(0).send(msg);

        } catch (RemoteException | IOException e) {
            e.printStackTrace();
        }
    }


    public void setService(ArtTransformService service) {
        mService = service;
    }


}
