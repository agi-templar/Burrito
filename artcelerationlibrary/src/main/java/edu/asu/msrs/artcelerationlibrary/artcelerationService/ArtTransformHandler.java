package edu.asu.msrs.artcelerationlibrary.artcelerationService;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.MemoryFile;
import android.os.Message;


import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;


import org.apache.commons.io.IOUtils;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.asu.msrs.artcelerationlibrary.MemoryFileUtil;
import edu.asu.msrs.artcelerationlibrary.TransformHandler;

public class ArtTransformHandler extends Handler implements TransformHandler{
    private ArtTransformService mService;
    static ArrayList<Messenger> mClients = new ArrayList<>();
    private Bitmap img_out;
    static Messenger replyTo;
    //public Messenger mArtTransformHandler = new Messenger(new ArtTransformHandler());


    @Override
    public void handleMessage(Message msg) {

        //new ArtTransformAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getBitmap(msg));
        replyTo = msg.replyTo;

        try {
            new ArtTransformAsyncTask().execute(getBitmap(msg));
        } catch (IOException e) {
            e.printStackTrace();
        }

        switch (msg.what){
            case 0:
                Log.d("doTransform", "Gaussian_Blur");

                break;
            case 1:
                Log.d("doTransform", "Neon_Edges");

                break;
            case 2:
                Log.d("doTransform", "Color_Filter");

                break;
            default:
                break;
        }


        //ArtTransformAsyncTask transformTask = new ArtTransformAsyncTask();

        Log.d("option", String.valueOf(msg.what));

        //transformTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, msg);


        //mService.stopSelf(msg.arg1);
    }

    public class ArtTransformAsyncTask extends AsyncTask <Bitmap, Integer, Void> {

        private Bitmap rawImage;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Bitmap... params) {

            rawImage = testTransform(params[0]);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            imageProcessed(rawImage);
        }

    }

    private Bitmap getBitmap(Message msg) throws IOException {
        Bundle dataBundle = msg.getData();
        ParcelFileDescriptor pfd = (ParcelFileDescriptor) dataBundle.get("pfd");
        InputStream inputStream = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
        //Convert the inputStream to bitmap
        byte[] byteArray =  IOUtils.toByteArray(inputStream);
        //The configuration is ARGB_8888, if the configuration changed in the application, here should be changed
        // a better way is to pass the parameter through the message.
        Bitmap.Config configBmp = Bitmap.Config.valueOf("ARGB_8888");
        Bitmap img = Bitmap.createBitmap(msg.arg1, msg.arg2, configBmp);
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        img.copyPixelsFromBuffer(buffer);
        return img;
    }


    private Bitmap testTransform(Bitmap img) {
        int width = img.getWidth();
        int height = img.getHeight();
        for(int x = width/4; x < width/4*3; x++)
        {
            for(int y = height/4; y < height/4*3; y++)
            {
                img.setPixel(x, y, Color.YELLOW);
            }
        }
        return img;
    }

    private void imageProcessed(Bitmap img){
        int width = img.getWidth();
        int height = img.getHeight();
        int what = 0;
        Message msg = Message.obtain(null, what,width,height);
        msg.replyTo = replyTo;

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
            //msg.obtain(null,6, 2, 3);
            mClients.get(0).send(msg);

        } catch (RemoteException | IOException e) {
            e.printStackTrace();
        }
    }

//    private void simulatingProcess(String type) {
//        long endTime = System.currentTimeMillis() + 5*1000;
//        while (System.currentTimeMillis() < endTime) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        Log.d("SimulatingProcess: ", type + " finished!");
//    }

    public void setService(ArtTransformService service) {
        mService = service;
    }

    @Override
    public void onTransformProcessed(Bitmap img_out) {
        //Log.d("artcelerationService", "TransformFinished");
    }

    class AsyncTest extends AsyncTask<Bitmap, Float, Bitmap> {
        //DONE IN BACKGROUND

        @Override
        protected Bitmap doInBackground(Bitmap...img) {
            return testTransform(img[0]);
        }

        //ON UI THREAD
        protected void onPostExecute(Bitmap mutableBitmap) {

            imageProcessed(mutableBitmap);
            Log.d("AsyncTest", "AsyncTest finished" );
        }
    }

}
