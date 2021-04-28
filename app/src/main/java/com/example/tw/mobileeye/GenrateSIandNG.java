package com.example.tw.mobileeye;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.InputFilter;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * Created by tw on 8/11/2016.
 */
public class GenrateSIandNG extends Service  {

    Context mContext;
    int batteryLevel, lac, cellId;
    SQLiteDatabase database;
    double latitude, longitude, speed;
    String date1, time1;
    String directionDegree;
    String strTime, strDate;
    int speedSI, tmD, dtD, cTM, pTM, cDT, pDT;
    float speed1, distanceKM;
    int interval, mOverSpeedLimit, aclimit, incidentLimit, lastCount, lastCountI, p, sNo1;
    String getLat, getLong, latDir, longDir, stampSI, stampJD, stampDV;
    String Time, AV, Lat, LatD, Lon, LongD, speedSt, DirDegree, Date, timeSt, pointID, routeId;
    GlobalVariable globalVariable;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        mContext = this;
        //Thread thread = new Thread(this);
        //thread.start();
        getMethod();
    }

    public void getMethod() {

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        globalVariable = (GlobalVariable) getApplicationContext();

        try {
            database = mContext.openOrCreateDatabase("Config", mContext.MODE_PRIVATE, null);
            database.execSQL("create table if not exists file(id INTEGER PRIMARY KEY AUTOINCREMENT,parameter varchar(100) unique,value varchar(150))");
            interval = retriveInt("siInterval");
            mOverSpeedLimit = retriveInt("osLimit");
            aclimit = retriveInt("acdcLimit");
            incidentLimit = retriveInt("incidentLimit");
            lastCount = retriveInt("STMdbSize");
            lastCountI = retriveInt("INCdbSize");
            database.close();
        } catch (Exception e) {
        }
        /*try {
            GpsService gps = new GpsService(mContext);
            if (gps.canGetLocation()) {
                latitude = gps.getLatitude();
                longitude = gps.getLongitude();
                if (latitude == 0.0) {
                    getLat = "0.0";
                } else {
                    getLat = convertToStandard(latitude);
                }
                if (longitude == 0.0) {
                    getLong = "0.0";
                } else {
                    getLong = convertToStandard(longitude);
                }

                if (latitude >= 0) {
                    latDir = "N";
                } else {
                    latDir = "S";
                }
                if (longitude >= 0) {
                    longDir = "E";
                } else {
                    longDir = "W";
                }

                speedSI = (int) ((gps.getSpeed() * 3600) / 1000);
                speed1 = (float) (gps.getSpeed() * 1.94384);
                speed = (float) ((gps.getSpeed() * 3600) / 1000);

                long time = gps.getTime();
                //Date date = new Date(time);
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");
                SimpleDateFormat stf = new SimpleDateFormat("HHmmss");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                stf.setTimeZone(TimeZone.getTimeZone("GMT"));
                date1 = sdf.format(date);
                time1 = stf.format(date);
            }
        } catch (Exception e){
        }*/

        File databaseExistD = mContext.getDatabasePath("GprmcDB");
        if (databaseExistD.exists()) {
            try {
                GprmcDatabase gd = new GprmcDatabase(mContext);
                timeSt = gd.getTime();
                AV = gd.getAV();
                getLat = gd.getLatitude();
                latDir = gd.getLatDir();
                getLong = gd.getLongitude();
                longDir = gd.getLongDir();
                speedSt = gd.getSpeed();
                directionDegree = gd.getDirDegree();
                date1 = gd.getDate();


                time1 = "" + timeSt;
            } catch (Exception e) {
            }
        }

        /*speedSt = ""+speedSt;
        speed = Double.parseDouble(speedSt);
        speed = speed * 1.852;*/

        /*float f = Float.parseFloat(speedSt);
        f = (float) (f * 1.852);
        Toast.makeText(GenrateSIandNG.this, ""+f, Toast.LENGTH_SHORT).show();*/

        /*try {
            LocationManager locationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                return;
            }
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
             directionDegree = location.getBearing();
        } catch (Exception e){
            Log.e("directionDegree", "" + e);
        }*/

        Intent batteryIntent = mContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        batteryLevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);


        File databaseExist16 = mContext.getDatabasePath("DistComp1.db");
        if (databaseExist16.exists()) {
            database = mContext.openOrCreateDatabase("DistComp1.db", mContext.MODE_PRIVATE, null);
            Cursor cr = database.rawQuery("Select * from DistCompTable1", null);
            cr.moveToFirst();
            while (!cr.isAfterLast()) {
                strDate = cr.getString(cr.getColumnIndex("DisDate1"));
                strTime = cr.getString(cr.getColumnIndex("DisTime1"));
                cr.moveToNext();
            }

            try {
                cDT = Integer.valueOf(date1);
                cTM = Integer.valueOf(time1);
                pDT = Integer.valueOf(strDate);
                pTM = Integer.valueOf(strTime);
                tmD = cTM - pTM;
                dtD = cDT - pDT;
            } catch (Exception e) {
            }

            database = mContext.openOrCreateDatabase("DistComp1.db", mContext.MODE_PRIVATE, null);
            database.execSQL("update DistCompTable1 set DisDate1 = '" + date1 + "'");
            database.execSQL("update DistCompTable1 set DisTime1 = '" + time1 + "'");
            database.close();
        } else {

            database = mContext.openOrCreateDatabase("DistComp1.db", mContext.MODE_PRIVATE, null);
            database.execSQL("Create table if not exists DistCompTable1(DisSpeed1 INTEGER(20), DisTime1 VARCHAR(20), DisDate1 VARCHAR(20))");
            database.execSQL("insert into DistCompTable1(DisDate1) values('" + date1 + "')");
            database.execSQL("insert into DistCompTable1(DisTime1) values('" + time1 + "')");
            database.close();
        }

        File databaseExist166 = mContext.getDatabasePath("DistComp2.db");
        if (databaseExist166.exists()) {
            database = mContext.openOrCreateDatabase("DistComp2.db", mContext.MODE_PRIVATE, null);
            Cursor cr = database.rawQuery("Select * from DistCompTable2", null);
            cr.moveToFirst();
            while (!cr.isAfterLast()) {
                distanceKM = cr.getFloat(cr.getColumnIndex("Distance2"));
                cr.moveToNext();
            }
        } else {
            distanceKM = 00;
        }

        try {
            cellId = 00;
            lac = 00;
            TelephonyManager telMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            int simState = telMgr.getSimState();
            if (simState == TelephonyManager.SIM_STATE_ABSENT) {
                cellId = 00;
                lac = 00;
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                GsmCellLocation gsmCellLocation = (GsmCellLocation) telMgr.getCellLocation();
                cellId = gsmCellLocation.getCid();
                lac = gsmCellLocation.getLac();
            }
        } catch (Exception e) {
        }

        /*StringTokenizer st = new StringTokenizer(time1, ".");
        String ptime = st.nextToken();
        Toast.makeText(GenrateSIandNG.this, ""+ptime, Toast.LENGTH_SHORT).show();*/

        try {
            pointID = globalVariable.getPointID(); //tr
        } catch (Exception e) {
            pointID = "00";
        }

        if (globalVariable.isJrmON() == true){
            if (cTM == pTM){
                stampSI = date1 + "," + time1 + "," + getLat + "," + latDir + "," + getLong + "," + longDir + "," + directionDegree + "," + speedSt + "," + distanceKM + ",0.0,V"+"\n"; //,$" + pointID+"," + batteryLevel + "," + cellId + "," + lac;
            } else {
                stampSI = date1 + "," + time1 + "," + getLat + "," + latDir + "," + getLong + "," + longDir + "," + directionDegree + "," + speedSt + "," + distanceKM + ",0.0,A"+"\n"; //,$" + pointID+"," + batteryLevel + "," + cellId + "," + lac;
            }
        } else {
            if (cTM == pTM){
                stampSI = date1 + "," + time1 + "," + getLat + "," + latDir + "," + getLong + "," + longDir + "," + directionDegree + "," + speedSt + "," + distanceKM + ",0.0,V"+"\n";//,$" + batteryLevel + "," + cellId + "," + lac;
            } else {
                stampSI = date1 + "," + time1 + "," + getLat + "," + latDir + "," + getLong + "," + longDir + "," + directionDegree + "," + speedSt + "," + distanceKM + ",0.0,A"+"\n";//,$" + batteryLevel + "," + cellId + "," + lac;
            }
        }
        routeId = globalVariable.getRouteNo();

        if (globalVariable.isJdStamp() == true){
            if (globalVariable.isJdGpsAV() == false){
                stampJD = "JD,"+globalVariable.getJdDateTime()+ "," + getLat + "," + latDir + "," + getLong + "," + longDir + "," + directionDegree + "," + speedSt + "," + distanceKM + "," + routeId + ",1,V";
            } else {
                stampJD = "JD,"+globalVariable.getJdDateTime() + "," + getLat + "," + latDir + "," + getLong + "," + longDir + "," + directionDegree + "," + speedSt + "," + distanceKM+ "," + routeId + ",1,A";
            }

            stampSI = stampSI+"\n"+stampJD;
            globalVariable.setJdStamp(false);
        }

        if (globalVariable.isDvFlag() == true)
        {
            if (globalVariable.isDvGpsAV() == false){
                stampDV = "DV,"+globalVariable.getDvDateTime()+ "," + getLat + "," + latDir + "," + getLong + "," + longDir + "," + directionDegree + "," + speedSt + "," + distanceKM + ",0.0,V";
            } else {
                stampDV = "DV,"+globalVariable.getDvDateTime() + "," + getLat + "," + latDir + "," + getLong + "," + longDir + "," + directionDegree + "," + speedSt + "," + distanceKM + ",0.0,A";
            }

            stampSI = stampSI+"\n"+stampDV;
            globalVariable.setDvFlag(false);

        }


        File databaseExist12 = mContext.getDatabasePath("lastData.db");
        if (databaseExist12.exists()) {
            database = mContext.openOrCreateDatabase("lastData.db", mContext.MODE_PRIVATE, null);
            database.execSQL("Create table if not exists lastDataTable(lastData VARCHAR(100))");
            database.execSQL("update lastDataTable set lastData = '" + stampSI + "'"+"\n");
            Log.e("lastData", "getMethod: " + stampSI);
            new MyLogger().storeMassage("lastData", "getMethod"+"SI"+stampSI);
            database.close();
        } else {
            database = mContext.openOrCreateDatabase("lastData.db", mContext.MODE_PRIVATE, null);
            database.execSQL("Create table if not exists lastDataTable(lastData VARCHAR(100))");
            database.execSQL("insert into lastDataTable(lastData) values('" + stampSI + "')"+"\n");
            Log.e("lastData", "getMethod: " +"SI"+","+ stampSI);
            database.close();
        }

        File databaseExist7 = mContext.getDatabasePath("stamps.db");
        if (databaseExist7.exists()) {
            database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
            Cursor cr7 = database.rawQuery("Select *from stampsTable", null);
            cr7.moveToFirst();
            while (!cr7.isAfterLast())
            {
                sNo1 = cr7.getInt(cr7.getColumnIndex("srNo"));
                cr7.moveToNext();
            }
            database.close();
            sNo1 = sNo1 + 1;
            int count = lastCount + 1;
            if (sNo1 == count){
                p = 1;
                File databaseExist = mContext.getDatabasePath("pointer.db");
                if (databaseExist.exists()) {
                    database = mContext.openOrCreateDatabase("pointer.db", mContext.MODE_PRIVATE, null);
                    Cursor cr8 = database.rawQuery("Select *from pointerTable", null);
                    cr8.moveToFirst();
                    while (!cr8.isAfterLast())
                    {
                        p = cr8.getInt(cr8.getColumnIndex("point"));
                        cr8.moveToNext();
                    }
                    database.close();
                }

                if (p <= lastCount) {
                    database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                    database.execSQL("update stampsTable set stamps = '" +"SI,"+ stampSI + "' where srNo = '" + p + "'");
                    database.close();
                    p = p + 1;
                    int count1 = lastCount + 1;
                    if (p == count1){
                        p = 1;
                    }
                    database = mContext.openOrCreateDatabase("pointer.db", mContext.MODE_PRIVATE, null);
                    database.execSQL("create table if not exists pointerTable (point INTEGER(10))");
                    database.execSQL("insert into pointerTable(point) values('" + p + "')");
                    database.close();
                }
            } else {
                database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                database.execSQL("create table if not exists stampsTable(srNo INTEGER PRIMARY KEY AUTOINCREMENT,stamps varchar(150))");
                database.execSQL("insert into stampsTable(stamps) values('" + "SI,"+ stampSI + "')" + "\n");
                database.close();
            }
        } else {
            database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
            database.execSQL("create table if not exists stampsTable(srNo INTEGER PRIMARY KEY AUTOINCREMENT,stamps varchar(150))");
            database.execSQL("insert into stampsTable(stamps) values('" + "SI,"+ stampSI + "')" + "\n");
            database.close();
        }

        LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (statusOfGPS == true){
        } else {
            String NG = "NG,"+stampSI;
            File databaseExist6 = mContext.getDatabasePath("stamps.db");
            if (databaseExist6.exists()) {
                database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                Cursor cr3 = database.rawQuery("Select *from stampsTable", null);
                cr3.moveToFirst();
                while (!cr3.isAfterLast())
                {
                    sNo1 = cr3.getInt(cr3.getColumnIndex("srNo"));
                    cr3.moveToNext();
                }
                database.close();
                sNo1 = sNo1 + 1;
                int count = lastCount + 1;
                if (sNo1 == count){
                    p = 1;
                    File databaseExist = mContext.getDatabasePath("pointer.db");
                    if (databaseExist.exists()) {
                        database = mContext.openOrCreateDatabase("pointer.db", mContext.MODE_PRIVATE, null);
                        Cursor cr4 = database.rawQuery("Select *from pointerTable", null);
                        cr4.moveToFirst();
                        while (!cr4.isAfterLast())
                        {
                            p = cr4.getInt(cr4.getColumnIndex("point"));
                            cr4.moveToNext();
                        }
                        database.close();
                    }

                    if (p <= lastCount) {
                        database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                        database.execSQL("update stampsTable set stamps = '" + NG + "' where srNo = '" + p + "'");
                        database.close();
                        p = p + 1;
                        int count1 = lastCount + 1;
                        if (p == count1){
                            p = 1;
                        }
                        database = mContext.openOrCreateDatabase("pointer.db", mContext.MODE_PRIVATE, null);
                        database.execSQL("create table if not exists pointerTable (point INTEGER(10))");
                        database.execSQL("insert into pointerTable(point) values('" + p + "')");
                        database.close();
                    }
                } else {
                    database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                    database.execSQL("create table if not exists stampsTable(srNo INTEGER PRIMARY KEY AUTOINCREMENT,stamps varchar(150))");
                    database.execSQL("insert into stampsTable(stamps) values('" + NG + "')" + "\n");
                    database.close();
                }
            } else {
                database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                database.execSQL("create table if not exists stampsTable(srNo INTEGER PRIMARY KEY AUTOINCREMENT,stamps varchar(150))");
                database.execSQL("insert into stampsTable(stamps) values('" + NG + "')" + "\n");
                database.close();
            }
        }

        String strOff = "OF,"+date1+","+time1+","+getLat+","+latDir+","+getLong+","+longDir+",0.0,"+speedSI+",0.0,0.0,A,$"+batteryLevel;

        File databaseExist155 = mContext.getDatabasePath("OffSt.db");
        if (databaseExist155.exists()) {
            database = mContext.openOrCreateDatabase("OffSt.db", mContext.MODE_PRIVATE, null);
            database.execSQL("update OffStTable set Offst = '" + strOff + "'");
            database.close();
        } else {
            database = mContext.openOrCreateDatabase("OffSt.db", mContext.MODE_PRIVATE, null);
            database.execSQL("Create table if not exists OffStTable(Offst VARCHAR)");
            String query4 = "INSERT INTO OffStTable (Offst) VALUES('"+strOff+"');";
            database.execSQL(query4);
            database.close();
        }

        new MyLogger().storeMassage("GenrateSIandNG", "SI stamps genrated");
        globalVariable.setSendnigFlage(true);
    }

    private Integer retriveInt(String p) {
        try {
            int res;
            Cursor c = database.rawQuery("select value from file where parameter='" + p + "'", null);
            c.moveToFirst();
            do {
                res = c.getInt(0);

            } while (c.moveToNext());
            return res;
        } catch (Exception e) {
        }
        return null;
    }

    public String convertToStandard(double x){
        long iPart;
        iPart = (long) x;
        String convertto4digit1 = "";
        double convertto4digit = ((iPart * 100) + ((x - iPart) * 60));
        DecimalFormat df = new DecimalFormat("####.0000");
        convertto4digit1 = df.format(convertto4digit);
        return convertto4digit1;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
