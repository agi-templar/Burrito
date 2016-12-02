package edu.asu.msrs.artcelerationlibrary.artcelerationService;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import edu.asu.msrs.artcelerationlibrary.R;

public class ArtTransformHandler extends Handler {
    private ArtTransformService mService;
    static ArrayList<Messenger> mClients = new ArrayList<>();
    static Messenger targetMessenger;
    List<ArtTransformAsyncTask> mArtTransformAsyncTasks;
    private int left, top, viewWidth, viewHeight;

    //Bitmap rawBitmap = BitmapFactory.decodeResource(this.getClass(), R.drawable.asuhayden, Bitmap.Config.ARGB_8888);

    static {
        System.loadLibrary("artTransform-lib");
    }

    public native void convertToGray(Bitmap bitmapIn, Bitmap bitmapOut);
    public native void lomo(Bitmap bitmapIn, Bitmap bitmapOut);
    //public native void findEdge(Bitmap bitmapIn, Bitmap bitmapOut);


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
                            rawBitmap = loadImage(params[0]);
                            newBitmap = rawBitmap.copy(Bitmap.Config.ARGB_8888, true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally{
                            lomo(rawBitmap,newBitmap);
                            //findEdge(bitmapNew,bitmapNew2);
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
                            newBitmap = changeLight(loadImage(params[0]));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 3:
                        try {
                            newBitmap = GaussianBlur(loadImage(params[0]));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 4:
                        try {
                            newBitmap = changeSaturation(loadImage(params[0]));
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
            notifyArtLib(newBitmap);

        }

    }


    /**
     * loadImage from message sent by ArtLib
     *
     * @param dataBundle
     * @return
     * @throws IOException
     */
//    private Bitmap loadImage(Bundle dataBundle) throws IOException {
//        //Bundle dataBundle = msg.getData();
//        ParcelFileDescriptor pfd = (ParcelFileDescriptor) dataBundle.get("pfd");
//        InputStream inputStream = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
//        //Convert the inputStream to bitmap
//        byte[] byteArray = IOUtils.toByteArray(inputStream);
//        //The configuration is ARGB_8888, if the configuration changed in the application, here should be changed
//        // a better way is to pass the parameter through the message.
//        //Bitmap.Config configBmp = Bitmap.Config.valueOf("ARGB_8888");
//        Bitmap rawBitmap = Bitmap.createBitmap(dataBundle.getInt("width"), dataBundle.getInt("height"), Bitmap.Config.ARGB_8888);
//        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
//        buffer.rewind();
//        rawBitmap.copyPixelsFromBuffer(buffer);
//        return rawBitmap;
//    }

    private Bitmap loadImage(Bundle dataBundle) throws IOException {
        left = dataBundle.getInt("left");
        top = dataBundle.getInt("top");
        viewWidth = dataBundle.getInt("viewWidth");
        viewHeight = dataBundle.getInt("viewHeight");
        return dataBundle.getParcelable("image");
    }


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

        tempBitmap = FastBlur.doBlur(tempBitmap, (int)radius, true);

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
     * do GaussianBlur
     *
     * @param img
     * @return
     */
//    private Bitmap GaussianBlur(Bitmap img) {
//
//        double[][] GaussianBlurMatrix = new double[][]{
//                {-1, 0, -1},
//                {0, 4, 0},
//                {-1, 0, -1}
//        };
//        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
//        convMatrix.applyConfig(GaussianBlurMatrix);
//        convMatrix.Factor = 1;
//        convMatrix.Offset = 127;
//        return ConvolutionMatrix.computeConvolution3x3(img, convMatrix);
//    }


    /**
     * when the artTransform is done, send a message to tell the ArtLib(client)
     *
     * @param img
     */
//    private void notifyArtLib(Bitmap img) {
//        int width = img.getWidth();
//        int height = img.getHeight();
//        int what = 0;
//        Message msg = Message.obtain(null, what, width, height);
//
//        msg.replyTo = targetMessenger;
//
//        //Message msg = Message.obtain(null, what);
//        Bundle dataBundle = new Bundle();
//        mClients.add(msg.replyTo);
//        if (msg.replyTo == null) {
//            Log.d("mclient is ", "null");
//        }
//        try {
//            int bytes = img.getByteCount();
//            ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
//            img.copyPixelsToBuffer(buffer); //Move the byte data to the buffer
//            byte[] byteArray = buffer.array();
//
//            MemoryFile memoryFile = new MemoryFile("someone", byteArray.length);
//            memoryFile.writeBytes(byteArray, 0, 0, byteArray.length);
//            ParcelFileDescriptor pfd = MemoryFileUtil.getParcelFileDescriptor(memoryFile);
//            memoryFile.close();
//
//            dataBundle.putParcelable("pfd", pfd);
//
//            msg.setData(dataBundle);
//            mClients.get(0).send(msg);
//
//        } catch (RemoteException | IOException e) {
//            e.printStackTrace();
//        }
//    }

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
