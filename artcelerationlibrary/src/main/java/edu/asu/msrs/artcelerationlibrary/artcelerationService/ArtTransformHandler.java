package edu.asu.msrs.artcelerationlibrary.artcelerationService;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;


import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;


import java.io.FileDescriptor;
import java.io.FileInputStream;

import edu.asu.msrs.artcelerationlibrary.TransformHandler;

public class ArtTransformHandler extends Handler implements TransformHandler{
    private ArtTransformService mService;
    private Bitmap img_out;

    @Override
    public void handleMessage(Message msg) {
        // To Do: handle message
        doTransform(msg.what);

        Log.d("option", String.valueOf(msg.what));

        Bundle bundle = msg.getData();
        ParcelFileDescriptor pfd = bundle.getParcelable("pfd");
        FileInputStream fios = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
        img_out = BitmapFactory.decodeStream(fios);


        //mService.stopSelf(msg.arg1);
    }

    private void doTransform(int option) {

        switch (option){
            case 0:
                Log.d("doTransform", "Gaussian_Blur");
                onTransformProcessed(img_out);
                break;
            case 1:
                Log.d("doTransform", "Neon_Edges");
                onTransformProcessed(img_out);
                break;
            case 2:
                Log.d("doTransform", "Color_Filter");
                onTransformProcessed(img_out);
                break;
            default:
                break;
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
        Log.d("artcelerationService", "TransformFinished");
    }
}
