package edu.asu.msrs.artcelerationlibrary.artcelerationService;


import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import edu.asu.msrs.artcelerationlibrary.TransformHandler;

public class ArtTransformHandler extends Handler implements TransformHandler{
    private ArtTransformService mService;

    @Override
    public void handleMessage(Message msg) {
        // To Do: handle message
        doTransform(msg.obj.toString());
        mService.stopSelf(msg.arg1);
    }

    private void doTransform(String option) {

        switch (option){
            case "0":
                Log.d("doTransform", "Gaussian_Blur");
                simulatingProcess("Gaussian_Blur");
                break;
            case "1":
                Log.d("doTransform", "Neon_Edges");
                simulatingProcess("Neon_Edges");
                break;
            case "2":
                Log.d("doTransform", "Color_Filter");
                simulatingProcess("Color_Filter");
                break;
            default:
                break;
        }
    }

    private void simulatingProcess(String type) {
        long endTime = System.currentTimeMillis() + 5*1000;
        while (System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d("SimulatingProcess: ", type + " finished!");
    }

    public void setService(ArtTransformService service) {
        mService = service;
    }

    @Override
    public void onTransformProcessed(Bitmap img_out) {

    }
}
