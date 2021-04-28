package com.example.tw.mobileeye;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;

/**
 * Created by twtech on 8/10/16.
 */
public class GpsLocationReceiver extends BroadcastReceiver {

    Method dataMtd;
    ConnectivityManager connMgr;
    ComponentName componentGps, componentGprs;

    @Override
    public void onReceive(Context context, Intent intent) {

        connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED"))
        {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                Log.e("GPS Available ", "YES");
                //Toast.makeText(context, "Start App", Toast.LENGTH_SHORT).show();
                Intent in = new Intent(context, MainActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(in);
            } else {
                Log.e("GPS Available ", "NO");
            }

        }
    }
}
