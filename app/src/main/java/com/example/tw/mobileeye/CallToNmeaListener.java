package com.example.tw.mobileeye;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by tw on 9/7/2016.
 */
public class CallToNmeaListener extends Service {

    Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = this;

        new MyLogger().storeMassage("CallToNmeaListener", "Called");

        GpsNmea gn = new GpsNmea(mContext);
        gn.getData();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
