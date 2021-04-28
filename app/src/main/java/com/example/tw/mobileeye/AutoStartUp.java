package com.example.tw.mobileeye;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class AutoStartUp extends Service {

    String strOff, strOn, stamps;
    SQLiteDatabase database;
    private int sNo1;
    int p;
    int lastCount, batteryLevel;
    int cellId, lac;
    TelephonyManager tel;
    GsmCellLocation gsmCellLocation;
    String getLat, getLong, latDir, longDir, directionDegree, date, timeSt, AV, speedSt;
    float distance;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        batteryLevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

        try {
            tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            gsmCellLocation = (GsmCellLocation) tel.getCellLocation();
            cellId = gsmCellLocation.getCid();
            lac = gsmCellLocation.getLac();
        } catch (Exception e){
        }


        Calendar caldar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
        SimpleDateFormat stf = new SimpleDateFormat("HHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        stf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date1 = sdf.format(caldar.getTime());
        String time1 = stf.format(caldar.getTime());

        File databaseExistD = getDatabasePath("GprmcDB");
        if (databaseExistD.exists())
        {
            try {
                GprmcDatabase gd = new GprmcDatabase(this);
                timeSt = gd.getTime();
                AV = gd.getAV();
                getLat = gd.getLatitude();
                latDir = gd.getLatDir();
                getLong = gd.getLongitude();
                longDir = gd.getLongDir();
                directionDegree = gd.getDirDegree();
                speedSt = gd.getSpeed();
                date = gd.getDate();
            } catch (Exception e){
            }
        }

        File databaseExist166 = getDatabasePath("DistComp2.db");
        if (databaseExist166.exists()) {

            try {
                database = openOrCreateDatabase("DistComp2.db", MODE_PRIVATE, null);
                Cursor cr = database.rawQuery("Select * from DistCompTable2", null);
                cr.moveToFirst();
                while (!cr.isAfterLast())
                {
                    distance = cr.getFloat(cr.getColumnIndex("Distance2"));
                    cr.moveToNext();
                }
            } catch (Exception e){
            }
        } else {
            distance = 00;
        }

        strOn = "ON,"+date1+","+time1+","+getLat+","+latDir+","+getLong+","+longDir+","+directionDegree+",0,"+distance+",0.0,V,$"+batteryLevel+","+cellId+","+lac;
        strOff = "OF,"+date+","+timeSt+","+getLat+","+latDir+","+getLong+","+longDir+","+directionDegree+",0,"+distance+",0.0,V,$"+batteryLevel+","+cellId+","+lac;
        stamps = strOff + "\n" + strOn;

        database = openOrCreateDatabase("Config", MODE_PRIVATE, null);
        database.execSQL("create table if not exists file(id INTEGER PRIMARY KEY AUTOINCREMENT,parameter varchar(100) unique,value varchar(150))");
        lastCount = retriveInt("STMdbSize");
        database.close();

        File databaseExist1 = getDatabasePath("stamps.db");
        if (databaseExist1.exists()) {
            database = openOrCreateDatabase("stamps.db", MODE_PRIVATE, null);
            Cursor cr = database.rawQuery("Select *from stampsTable", null);
            cr.moveToFirst();
            while (!cr.isAfterLast())
            {
                sNo1 = cr.getInt(cr.getColumnIndex("srNo"));
                cr.moveToNext();
            }
            database.close();
            sNo1 = sNo1 + 1;
            int count = lastCount + 1;
            if (sNo1 == count){
                p = 1;
                File databaseExist = getDatabasePath("pointer.db");
                if (databaseExist.exists()) {
                    database = openOrCreateDatabase("pointer.db", MODE_PRIVATE, null);
                    Cursor cr1 = database.rawQuery("Select *from pointerTable", null);
                    cr1.moveToFirst();
                    while (!cr1.isAfterLast())
                    {
                        p = cr1.getInt(cr1.getColumnIndex("point"));
                        cr1.moveToNext();
                    }
                    database.close();
                }

                if (p <= lastCount) {
                    database = openOrCreateDatabase("stamps.db", MODE_PRIVATE, null);
                    database.execSQL("update stampsTable set stamps = '" + stamps + "' where srNo = '" + p + "'");
                    database.close();
                    p = p + 1;
                    int count1 = lastCount + 1;
                    if (p == count1){
                        p = 1;
                    }

                    database = openOrCreateDatabase("pointer.db", MODE_PRIVATE, null);
                    database.execSQL("create table if not exists pointerTable (point INTEGER(10))");
                    database.execSQL("insert into pointerTable(point) values('" + p + "')");
                    database.close();
                }
            } else {
                database = openOrCreateDatabase("stamps.db", MODE_PRIVATE, null);
                database.execSQL("create table if not exists stampsTable(srNo INTEGER PRIMARY KEY AUTOINCREMENT,stamps varchar(150))");
                database.execSQL("insert into stampsTable(stamps) values('" + stamps + "')" + "\n");
                database.close();
            }
        } else {
            database = openOrCreateDatabase("stamps.db", MODE_PRIVATE, null);
            database.execSQL("create table if not exists stampsTable(srNo INTEGER PRIMARY KEY AUTOINCREMENT,stamps varchar(150))");
            database.execSQL("insert into stampsTable(stamps) values('" + stamps + "')" + "\n");
            database.close();
        }
        // For show the app icon...
       /* PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, com.example.tw.mobileeye.MainActivity.class);
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);*/

        Intent i = new Intent(AutoStartUp.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private Integer retriveInt(String p) {
        try {
            int res;
            //query to retrieve the data
            Cursor c = database.rawQuery("select value from file where parameter='" + p + "'", null);
            c.moveToFirst();
            do {
                res = c.getInt(0);
            } while (c.moveToNext());
            return res;
        } catch (Exception e) {
            //return "error" + e.getMessage();
        }
        return null;
    }
}