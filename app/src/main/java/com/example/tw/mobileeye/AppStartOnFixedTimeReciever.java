package com.example.tw.mobileeye;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class AppStartOnFixedTimeReciever extends BroadcastReceiver {

    Intent serviceIntent;

    @Override
    public void onReceive(Context context, Intent intent) {

        new MyLogger().storeMassage("AppStartOnFixedTimeReciever", "onReceive called");

        try {
            context.startService(new Intent(context, TimeForCall2Features.class));
            Log.e("MyReciver", "SdReciver");

            /*serviceIntent = new Intent(context,JRMService.class);
            serviceIntent.putExtra("RouteNo", "Route113");
            context.startService(serviceIntent);*/

        } catch (Exception e){
            Log.e("MyReciver", "onReceive: " + e.getMessage() );

        }
        Log.e("MyReciver", "SdReciver2");
    }
}
