package com.example.tw.mobileeye;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by twtech on 14/10/16.
 */
public class StartActivity extends AppCompatActivity {

    Method dataMtd;
    ConnectivityManager connMgr;
    LocationManager locationManager;
    NetworkInfo netInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        connMgr = ( ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        {
            netInfo = connMgr.getActiveNetworkInfo();
            if ((netInfo == null) || (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {

                AlertDialog.Builder adb = new AlertDialog.Builder(StartActivity.this);
                adb.setTitle("Please Turn On");
                adb.setMessage("App is requried to turn on GPS & Internet");
                adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (netInfo != null) {
                            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                                //Toast.makeText(StartActivity.this, "Start App", Toast.LENGTH_SHORT).show();
                                Intent in = new Intent(StartActivity.this, MainActivity.class);
                                startActivity(in);
                            } else {

                                Intent in = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(in);
                            }
                        } else {
                            Log.e("Net", "Off");
                            try {
                                DataOn();
                            } catch (Exception e){
                                Intent in = new Intent(Intent.ACTION_MAIN);
                                in.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(in);
                            }
                        }
                    }
                });
                AlertDialog ad = adb.create();
                ad.show();

            } else {
                //Toast.makeText(StartActivity.this, "Start App", Toast.LENGTH_SHORT).show();
                Intent in = new Intent(StartActivity.this, MainActivity.class);
                startActivity(in);
                StartActivity.this.finish();
            }
        }
    }

    public void DataOn()
    {
        try {
            dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        dataMtd.setAccessible(true);

        try {
            dataMtd.invoke(connMgr, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
