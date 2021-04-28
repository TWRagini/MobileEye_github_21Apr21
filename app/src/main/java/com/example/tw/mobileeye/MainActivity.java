package com.example.tw.mobileeye;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    String unitID, tfStamp;
    String imeiNo, simNo, ver;
    int cellId, lac;
    TelephonyManager tel;
    GsmCellLocation gsmCellLocation;
    int batteryLevel;
    SQLiteDatabase database, sql;
    String stamps;
    private int sNo1;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
  // String databaseName = Environment.getExternalStorageDirectory().getPath() + "/TWJRmDB/SiSt.db";
   // String databaseName2 = Environment.getExternalStorageDirectory().getPath() + "/TWJRmDB/dist_comp.db";
    //String databaseName3 = Environment.getExternalStorageDirectory().getPath() + "/TWJRmDB/SiSt.db";
    int p;
    int lastCount;
    File myFile = new File("/sdcard/Logs_MobileEyeAppData.txt");
    GlobalVariable globalVariable;
    String getLat, getLong, latDir, longDir, directionDegree;
    float distance;
    ComponentName componentGps, componentGprs; // Identifier for a specific application component

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int readStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int phoneStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
            int internetPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
            int accessNetworkPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
            int getAccountsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
            int readCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

            int readSMSPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);

            List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (readStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (readCameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (writeStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (phoneStatePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (internetPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET);
        }
        if (accessNetworkPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if (getAccountsPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.GET_ACCOUNTS);
        }
       /* if (readSMSPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_SMS);
        }*/
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);

            }


        Log.e("Main", "http://103.241.181.36:8080/DeviceIMEITracker/test?deviceId=351894091287012");

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Log.e("Main", "Before GPS" );
        componentGps = new ComponentName(MainActivity.this, GpsLocationReceiver.class);
        Log.e("Main", "After GPS" );
        componentGprs = new ComponentName(MainActivity.this, NetworkChangeReceiver.class);
        //This component or application has been explicitly disabled, regardless of what it has specified in its manifest. ,(DONT_KILL_APP)to indicate that you don't want to kill the app containing the component.
        getPackageManager().setComponentEnabledSetting(componentGps, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        getPackageManager().setComponentEnabledSetting(componentGprs, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        // PackageManager Class for retrieving various kinds of information related to the application packages
        new MyLogger().storeMassage("MainActivity", "Application Start");

        globalVariable = (GlobalVariable) getApplicationContext();

        if (globalVariable.isJrmStopCalled()) {
            globalVariable.setJrmStopCalled(false);
            MainActivity.this.finish();
        } else {

            globalVariable.setTFSendingFlage(true);
            globalVariable.setSendnigFlage(false);

            // For hide app icon
       /* PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, com.example.tw.mobileeye.MainActivity.class);
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);*/

            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                getPermission();
            } else {

                doActivity();
            }
        }
        // finish();
    }

    public void doActivity() {

        try {
            File databaseExist6 = getApplicationContext().getDatabasePath("Config");
            if (databaseExist6.exists()) {
                database = openOrCreateDatabase("Config", MODE_PRIVATE, null);
                database.execSQL("create table if not exists file(id INTEGER PRIMARY KEY AUTOINCREMENT,parameter varchar(100) unique,value varchar(150))");
                lastCount = retriveInt("STMdbSize");
                database.close();
            } else {
                lastCount = 100000;
            }
        } catch (Exception e) {
        }

        try {

            //sql = openOrCreateDatabase(databaseName, MODE_PRIVATE, null);
            sql = openOrCreateDatabase("SiSt.db", MODE_PRIVATE, null);
            sql.execSQL("create table if not exists counter(count int,m_counter int,curr_speed int,max_speed int, ospeed varchar(20),over_speed varchar(20))");
            String count = "SELECT count(*) FROM counter";
            Cursor mcursor = sql.rawQuery(count, null);
            mcursor.moveToFirst();
            int icount = mcursor.getInt(0);
            if (icount > 0) {
                Log.v("Mylog", "inside if");
            } else {
                sql.execSQL("insert into counter values(0,0,0,0,'false','false')");
            }
            sql.close();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        try {
            sql = openOrCreateDatabase("dist_comp", MODE_PRIVATE, null);
            sql.execSQL("create table if not exists distance(dist float)");
            String count = "SELECT count(*) FROM distance";
            Cursor mcursor = sql.rawQuery(count, null);

            mcursor.moveToFirst();
            int icount = mcursor.getInt(0);
            if (icount > 0) {
                Log.v("Mylog", "inside if");
            } else {
                sql.execSQL("insert into distance values(" + 0 + ")");
                sql.close();
            }
        } catch (Exception e) {
            Log.e("dist_comp", "" + e);
        }

        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        batteryLevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0); // getting the packageName
            Log.e("Main", "pInfo: "+ pInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        ver = pInfo.versionName;  // getting version name of app here
        tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        int simState = tel.getSimState();
        Log.e("Main", "simState : "+ simState );

        try {
            if (simState == TelephonyManager.SIM_STATE_ABSENT) {
                simNo = "00";
                imeiNo = "00";
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                //simNo = tel.getSimSerialNumber().toString();
                imeiNo = tel.getDeviceId().toString();
                Log.e("Main", "http://103.241.181.36:8080/DeviceIMEITracker/test?deviceId="+imeiNo);
                gsmCellLocation = (GsmCellLocation) tel.getCellLocation();
                cellId = gsmCellLocation.getCid();
                lac = gsmCellLocation.getLac();
                Log.e("Main", "simState,simNo,imeiNo,cellId,lac  : "+ simState+","+simNo+","+imeiNo+"," +cellId+","+lac);
            }
        } catch (Exception e ){
            imeiNo = "00";
            simNo = "00";
        }

        tfStamp = "TF,CV:"+ver+",SM:"+simNo+",IMEI:"+imeiNo;

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
                getLat = gd.getLatitude();
                latDir = gd.getLatDir();
                getLong = gd.getLongitude();
                longDir = gd.getLongDir();
                directionDegree = gd.getDirDegree();
            } catch (Exception e){
            }
        } else {
            getLat = "0.0";
            latDir = "0";
            getLong = "0.0";
            longDir = "0";
            directionDegree = "0.0";
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

        stamps = "ER,"+date1+","+time1+","+getLat+","+latDir+","+getLong+","+longDir+","+directionDegree+",0,"+distance+",0.0,V,$"+batteryLevel+","+cellId+","+lac;
        Log.e("Main", "Stamps: " + stamps);


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

        File databaseExist5 = getApplicationContext().getDatabasePath("Config");
        if (!databaseExist5.exists()) {
            Log.i("Database", "Not Found");
            database = openOrCreateDatabase("Config", MODE_PRIVATE, null);
            database.execSQL("create table if not exists file(id INTEGER PRIMARY KEY AUTOINCREMENT,parameter varchar(100) unique,value varchar(150))");
            insert("imeiNo", imeiNo);
            insert("unitId", "0000");
            insert("smtpHost", "a.mobileeye.in");
            insert("smtpPort", "25");
            insert("smtpPW", "transworld");
            insert("toMail", "g1@a.mobileeye.in");
            //insert("ftpHost", "images.mobileeye.in");
            insert("ftpHost", "tw.mobileeye.in");
            insert("ftpPort", "21");
            insert("ftpUN", "cameraimages");
            insert("ftpPW", "Cimages@123");
            insert("ftpPath", "/Android/");
            insert("toMailIncident", "avlincident@twphd.in");
            insert("uIdUrl", "http://103.241.181.36:8080/DeviceIMEITracker/test?deviceId=");
            insertInt("siInterval", 60);
            insertInt("siDuration", 120);
            insertInt("acdcLimit", 15);
            insertInt("rZoneOs", 30);
            insertInt("yZoneOs", 40);
            insertInt("gZoneOs", 65);
            insertInt("osLimit", 50);
            insertInt("incidentLimit", 3);
            insertInt("STMdbSize", 100000);
            insertInt("INCdbSize", 1800);
            insertInt("STMmailSize", 128);
            insertInt("INCmailSize", 170);
            insert("hostCommand", "pop.mobile-eye.in");
            insert("portCommand", "995");
            insert("unCommand", "@mobile-eye.in");
            insert("pwCommand", "transworld@123");
            insert("hostRoute", "pop.mobile-eye.in");
            insert("portRoute", "995");
            insert("unRoute", "jrmroute@mobile-eye.in");
            insert("pwRoute", "jrmroute@123");
            database.close();

            Log.i("Database", "Created");
        }

        ConnectivityManager conMgr = ( ConnectivityManager)  getSystemService(Context.CONNECTIVITY_SERVICE);
        {
            NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
            if (netInfo == null) {
            } else {
                MyClass myClass = new MyClass();
                myClass.execute();
            }
        }
///////////////////////////////////////////////////////////////////////////////////////////////////////
        new Thread(new Runnable() {

            @Override
            public void run() {

                Intent myIntent1 = new Intent(getBaseContext(), AppStartOnFixedTimeReciever.class);
                //myIntent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pendingIntent1 = PendingIntent.getBroadcast(getBaseContext(), 0, myIntent1, 0);
                //@SuppressLint("WrongConstant") PendingIntent pendingIntent1 = PendingIntent.getBroadcast(getBaseContext(), 0, myIntent1, 1);
                AlarmManager alarmManager1 = (AlarmManager) getSystemService(ALARM_SERVICE);
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTimeInMillis(System.currentTimeMillis());
                calendar1.add(Calendar.SECOND, 1);
                alarmManager1.setRepeating(AlarmManager.RTC_WAKEUP, calendar1.getTimeInMillis(), 5 * 60 * 1000, pendingIntent1);



                MainActivity.this.finish();


            }
        }).start();




        /*Intent jrmIntent = new Intent(MainActivity.this, JRMTestingActivity.class);
        startActivity(jrmIntent);*/
    }

    private class MyClass extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mWeb();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            database = openOrCreateDatabase("Config", MODE_PRIVATE, null);
            database.execSQL("create table if not exists file(id INTEGER PRIMARY KEY AUTOINCREMENT,parameter varchar(100) unique,value varchar(150))");
            update("unitId", unitID);
            Log.e("Main", "onPostExecute: ");
            database.close();
            Toast.makeText(getApplicationContext(), ""+unitID, Toast.LENGTH_SHORT).show();

        }
    }

    private void mWeb() {
        database = openOrCreateDatabase("Config", MODE_PRIVATE, null);
        database.execSQL("create table if not exists file(id INTEGER PRIMARY KEY AUTOINCREMENT,parameter varchar(100) unique,value varchar(150))");
        try {
            String uIdurl = retrive("uIdUrl");
            String strurl= uIdurl+imeiNo;
                URL urlRef = new URL(strurl);
            Log.e("Main", "new url: " + urlRef);
                URLConnection conn = urlRef.openConnection();
            Log.e("Main", "url connection done");
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String temp = "";
                StringBuilder buffer = new StringBuilder("");
                while ((temp = reader.readLine()) != null) {
                    buffer.append(temp);
            }
            unitID = buffer.toString();
            System.out.println("unitID is " + unitID);
            Log.e("Main", "unitID is " + unitID);
        } catch (Exception e){
            e.printStackTrace();
            Log.e("Main", "mWeb: " + e.getMessage() );
        }
        database.close();
    }

    private String insert(String p, String v){
        try {
            database.execSQL("insert into file(parameter,value) values('" + p + "','" + v + "')");
            return "Success";
        } catch (Exception e){
            return e.getMessage();
        }
    }

    private String insertInt(String p, int v) {
        try {
            database.execSQL("insert into file(parameter,value) values('" + p + "','" + v + "')");
            return "Success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String update(String p, String v) {
        try {
            database.execSQL("update file set value='" + v + "' where parameter ='" + p + "'");
            return "Success";
        } catch (Exception e) {
            return e.getMessage();
        }
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

    private String retrive(String p) {
        try {
            String res;
            Cursor c = database.rawQuery("select value from file where parameter='" + p + "'", null);
            c.moveToFirst();
            do {
                res = c.getString(0);
            } while (c.moveToNext());
            return res;
        } catch (Exception e) {
            return "error" + e.getMessage();
        }
    }

    public void getPermission(){

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION))
            {       // Show an expanation to the user *asynchronously* -- don't block this thread waiting for the user's response! After the user sees the explanation, try again to request the permission.
            } else
            {       // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an app-defined int constant. The callback method gets the result of the request.
            }
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            {       // Show an expanation to the user *asynchronously* -- don't block this thread waiting for the user's response! After the user sees the explanation, try again to request the permission.
            } else
            {       // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an app-defined int constant. The callback method gets the result of the request.
            }
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
        {            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_PHONE_STATE))
            {       // Show an expanation to the user *asynchronously* -- don't block this thread waiting for the user's response! After the user sees the explanation, try again to request the permission.
            } else
            {       // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 3);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an app-defined int constant. The callback method gets the result of the request.
            }
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED)
        {            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.GET_ACCOUNTS))
            {       // Show an expanation to the user *asynchronously* -- don't block this thread waiting for the user's response! After the user sees the explanation, try again to request the permission.
            } else
            {       // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.GET_ACCOUNTS}, 4);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an app-defined int constant. The callback method gets the result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 3: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   // Toast.makeText(MainActivity.this, "Phone permission was granted", Toast.LENGTH_SHORT).show();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    doActivity();
                } else {
                    //Toast.makeText(MainActivity.this, "Phone permission denied", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
