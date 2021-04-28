package com.example.tw.mobileeye;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by twtech on 8/10/16.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    LocationManager locationManager;
    ComponentName componentGps, componentGprs;

    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        boolean isConnected = wifi != null && wifi.isConnectedOrConnecting() || mobile != null && mobile.isConnectedOrConnecting();
        if (isConnected) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Log.e("GPRS Available ", "YES");

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                //Toast.makeText(context, "Start App", Toast.LENGTH_SHORT).show();
                Intent inn = new Intent(context, MainActivity.class);
                inn.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(inn);
            }
            else
            {
                Intent in = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(in);
            }

        } else {
            Log.e("GPRS Available ", "NO");
        }
    }
}
