package edu.asu.msrs.artcelerationlibrary.artcelerationService;


import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
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
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import edu.asu.msrs.artcelerationlibrary.MemoryFileUtil;

public class ArtTransformHandler extends Handler {
    private ArtTransformService mService;
    static ArrayList<Messenger> mClients = new ArrayList<>();
    static Messenger targetMessenger;
    List<ArtTransformAsyncTask> mArtTransformAsyncTasks;

    public native void convertToGray(Bitmap bitmapIn,Bitmap bitmapOut);


    @Override
    public void handleMessage(Message msg) {

        targetMessenger = msg.replyTo;
        mArtTransformAsyncTasks = new ArrayList<>();
        Bundle dataBundle = msg.getData();
        new ArtTransformAsyncTask().executeOnExecutor(Executors.newCachedThreadPool(), dataBundle);

    }

    public class ArtTransformAsyncTask extends AsyncTask<Bundle, Void, Void> {

        private Bitmap rawBitmap;

        @Override
        protected void onPreExecute() {
            mArtTransformAsyncTasks.add(this);
        }

        @Override
        protected Void doInBackground(Bundle... params) {


            rawBitmap = Bitmap.createBitmap(1600,1066, Bitmap.Config.valueOf("ARGB_8888"));

            Log.d("Message", String.valueOf(params[0]));

                switch (params[0].getInt("index")) {

                    case 0:
                        try {
                            convertToGray(loadImage(params[0]),rawBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1:
                        try {
                            rawBitmap = changeHSL(loadImage(params[0]));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 2:
                        try {
                            rawBitmap = changeLight(loadImage(params[0]));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 3:
                        try {
                            rawBitmap = GaussianBlur(loadImage(params[0]));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 4:
                        try {
                            rawBitmap = changeSaturation(loadImage(params[0]));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;

                }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            mArtTransformAsyncTasks.remove(this);
            if (mArtTransformAsyncTasks.size() == 0) {
                Log.d("AsyncTask", "All Tasks Finished");
            }
            notifyArtLib(rawBitmap);

        }

    }


    /**
     * loadImage from message sent by ArtLib
     *
     * @param dataBundle
     * @return
     * @throws IOException
     */
    private Bitmap loadImage(Bundle dataBundle) throws IOException {
        //Bundle dataBundle = msg.getData();
        ParcelFileDescriptor pfd = (ParcelFileDescriptor) dataBundle.get("pfd");
        InputStream inputStream = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
        //Convert the inputStream to bitmap
        byte[] byteArray = IOUtils.toByteArray(inputStream);
        //The configuration is ARGB_8888, if the configuration changed in the application, here should be changed
        // a better way is to pass the parameter through the message.
        Bitmap.Config configBmp = Bitmap.Config.valueOf("ARGB_8888");
        Bitmap rawBitmap = Bitmap.createBitmap(dataBundle.getInt("width"), dataBundle.getInt("height"), configBmp);
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        buffer.rewind();
        rawBitmap.copyPixelsFromBuffer(buffer);
        return rawBitmap;
    }


    /**
     * change the Saturation
     *
     * @param img
     * @return
     */
    private Bitmap changeSaturation(Bitmap img) {
        ColorMatrix colorMatrixSaturation = new ColorMatrix();
        ColorMatrix allColorMatrix = new ColorMatrix();

        colorMatrixSaturation.reset();
        colorMatrixSaturation.setSaturation(0.40f);

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

    /**
     * change the RGB value
     *
     * @param img
     * @return
     */
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

    /**
     * change the light
     *
     * @param img
     * @return
     */
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

    /**
     * do GaussianBlur
     *
     * @param img
     * @return
     */
    private Bitmap GaussianBlur(Bitmap img) {

        double[][] GaussianBlurMatrix = new double[][]{
                {1, 2, 1},
                {2, 4, 2},
                {1, 2, 1}
        };
        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
        convMatrix.applyConfig(GaussianBlurMatrix);
        convMatrix.Factor = 16;
        convMatrix.Offset = 0;
        return ConvolutionMatrix.computeConvolution3x3(img, convMatrix);
    }


    /**
     * when the artTransform is done, send a message to tell the ArtLib(client)
     *
     * @param img
     */
    private void notifyArtLib(Bitmap img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int what = 0;
        Message msg = Message.obtain(null, what, width, height);
        long tsLong = System.currentTimeMillis() / 1000;
        //msg.what = (int)tsLong;
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
            buffer.rewind();
            img.copyPixelsToBuffer(buffer); //Move the byte data to the buffer
            byte[] byteArray = buffer.array();

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
