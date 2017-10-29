package edu.dartmouth.cs65.artcelerationlibrary.artcelerationService;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;


import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ArtTransformHandler extends Handler {
    private ArtTransformService mService;
    static ArrayList<Messenger> mClients = new ArrayList<>();
    static Messenger targetMessenger;
    List<ArtTransformAsyncTask> mArtTransformAsyncTasks;
    private int left, top, viewWidth, viewHeight;

    static {
        System.loadLibrary("artTransform-lib");
    }

    public native void filter(Bitmap bitmapIn, Bitmap bitmapOut);
    public native void lomo(Bitmap bitmapIn, Bitmap bitmapOut);
    public native void grey(Bitmap bitmapIn, Bitmap bitmapOut);

    @Override
    public void handleMessage(Message msg) {

        targetMessenger = msg.replyTo;
        mArtTransformAsyncTasks = new ArrayList<>();
        Bundle dataBundle = msg.getData();
        new ArtTransformAsyncTask().executeOnExecutor(Executors.newCachedThreadPool(), dataBundle);

    }

    public class ArtTransformAsyncTask extends AsyncTask<Bundle, Void, Void> {

        Bitmap rawBitmap;
        Bitmap newBitmap;

        @Override
        protected void onPreExecute() {
            mArtTransformAsyncTasks.add(this);
        }

        @Override
        protected Void doInBackground(Bundle... params) {

            Log.d("Message", String.valueOf(params[0]));

                switch (params[0].getInt("index")) {

                    case 0:
                        try {
                            newBitmap = GaussianBlur(loadImage(params[0]));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1:
                        try {
                            newBitmap = changeHSL(loadImage(params[0]));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 2:

                        try {
                            rawBitmap = loadImage(params[0]);
                            newBitmap = rawBitmap.copy(Bitmap.Config.ARGB_8888, true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            filter(rawBitmap, newBitmap);
                        }

                        break;
                    case 3:
                        try {
                            rawBitmap = loadImage(params[0]);
                            newBitmap = rawBitmap.copy(Bitmap.Config.ARGB_8888, true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            lomo(rawBitmap, newBitmap);
                        }
                        break;
                    case 4:
                        try {
                            rawBitmap = loadImage(params[0]);
                            newBitmap = rawBitmap.copy(Bitmap.Config.ARGB_8888, true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            grey(rawBitmap, newBitmap);
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
            notifyArtLib(newBitmap);
        }
    }


    /**
     *
     * loadImage from message sent by ArtLib
     *
     * @param dataBundle
     * @return
     * @throws IOException
     */
    private Bitmap loadImage(Bundle dataBundle) throws IOException {
        left = dataBundle.getInt("left");
        top = dataBundle.getInt("top");
        viewWidth = dataBundle.getInt("viewWidth");
        viewHeight = dataBundle.getInt("viewHeight");
        return dataBundle.getParcelable("image");
    }


    /**
     * Gaussian Blur
     *
     * @param img
     * @return
     */
    private Bitmap GaussianBlur(Bitmap img) {
        float scaleFactor = 3;
        float radius = 8;
        Bitmap tempBitmap = Bitmap.createBitmap((int)(viewWidth/scaleFactor), (int)(1066/scaleFactor), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(tempBitmap);
        canvas.translate(0, 0);

        canvas.scale(1/scaleFactor,1/scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(img, 0, 0, paint);

        tempBitmap = GaussianBlur.doBlur(tempBitmap, (int) radius, true);

        return Bitmap.createScaledBitmap(tempBitmap, 1200, 1066, false);
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
     *
     * when the artTransform is done, send a message to tell the ArtLib(client)
     *
     * @param img
     */
    private void notifyArtLib(Bitmap img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int what = 0;
        Message msg = Message.obtain(null, what, width, height);

        msg.replyTo = targetMessenger;

        //Message msg = Message.obtain(null, what);
        Bundle dataBundle = new Bundle();
        mClients.add(msg.replyTo);

        if (msg.replyTo == null) {
            Log.d("mclient is ", "null");
        }

        dataBundle.putParcelable("image", img);
        msg.setData(dataBundle);

        try {
            mClients.get(0).send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    public void setService(ArtTransformService service) {
        mService = service;
    }


}
