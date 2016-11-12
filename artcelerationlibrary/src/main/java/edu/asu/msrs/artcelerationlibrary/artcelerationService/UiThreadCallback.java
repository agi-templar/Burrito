package edu.asu.msrs.artcelerationlibrary.artcelerationService;


import android.os.Message;

public interface UiThreadCallback {
    void publishToUiThread(Message message);
}
