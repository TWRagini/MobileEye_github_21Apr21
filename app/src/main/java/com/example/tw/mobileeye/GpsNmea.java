package com.example.tw.mobileeye;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import static android.content.ContentValues.TAG;


public class GpsNmea implements LocationListener {

    Context mContext;
    LocationManager lm;
    long lastNmeaTime;
    String lastNmeaMessage, stNmea, rmc;
    String pTime, pAV, pLat, pLatD, pLong, pLongD, pSpped, pDirDgree, pDate;
    String Time, AV, Lat, LatD, Lon, LongD, Speed, DirDegree, Date, time1;
    SQLiteDatabase sqliteDatabase, sqLiteDatabase1, database;
    String timeString, prev_lat_str, prev_long_str, stampOS, strB, stampRMC, pointId;
    long timediff;
    double prev_lat, prev_long, latitude, longitude;
    float perv_speed, speedFloat;
    int curr_ac_limit, mCountOS, mCountOS1min, mOverspeedA, mOverspeedB, mOverSpeedLimit ,sNo1, sNo2;
    static boolean mOverspd, mOSpeedFlag;
    int aclimit, lastCount, interval, incidentLimit, lastCountI, p, pI, icSpeed, ipSpeed;
    GlobalVariable globalVaribale;
    int greenZoneOsLimit, yellowZoneOsLimit, redZoneOsLimit, osAlert=16;
    MediaPlayer mediaPlayer;


    public GpsNmea(Context mContext) {
        this.mContext = mContext;
        mediaPlayer = MediaPlayer.create(mContext, R.raw.over_speeding_reduce_speed_to_30);
    }

    public void getData(){

        try {
            database = mContext.openOrCreateDatabase("Config", mContext.MODE_PRIVATE, null);
            database.execSQL("create table if not exists file(id INTEGER PRIMARY KEY AUTOINCREMENT,parameter varchar(100) unique,value varchar(150))");
            interval = retriveInt("siInterval");
            mOverSpeedLimit = retriveInt("osLimit");
            aclimit = retriveInt("acdcLimit");
            incidentLimit = retriveInt("incidentLimit");
            lastCount = retriveInt("STMdbSize");
            lastCountI = retriveInt("INCdbSize");
            redZoneOsLimit = retriveInt("rZoneOs");
            yellowZoneOsLimit = retriveInt("yZoneOs");
            greenZoneOsLimit = retriveInt("gZoneOs");
            database.close();
        } catch (Exception e){
        }

        globalVaribale = (GlobalVariable) mContext.getApplicationContext();

        lm = (LocationManager) mContext.getApplicationContext().getSystemService(mContext.LOCATION_SERVICE);

        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else {
            Log.e("NmeaListener", "GPS PROVIDER is unavailable");
        }

        ////////////////////////////

        /////////////////////////////////

        lm.addNmeaListener(new GpsStatus.NmeaListener() {
            @Override
            public void onNmeaReceived(long timestamp, String nmea) {

                GprmcDatabase gd = new GprmcDatabase(mContext);

                File databaseExist = mContext.getDatabasePath("GprmcDB");
                if (databaseExist.exists()) {
                    try {
                        Time = gd.getTime();
                        AV = gd.getAV();
                        Lat = gd.getLatitude();
                        LatD = gd.getLatDir();
                        Lon = gd.getLongitude();
                        LongD = gd.getLongDir();
                        Speed = gd.getSpeed();
                        DirDegree = gd.getDirDegree();
                        Date = gd.getDate();
                    } catch (Exception e){
                    }
                    /*String pSp2 = ""+Speed;
                    StringTokenizer sts2 = new StringTokenizer(pSp2, ".");
                    String pSp3 = sts2.nextToken();*/
                    try {
                        ipSpeed = Integer.parseInt(Speed);
                    } catch (Exception e){
                    }
                }

                lastNmeaTime = timestamp;

                lastNmeaMessage = nmea;
                Log.e("GpsNmea", "lastNmeaMessage " + lastNmeaMessage);

                StringTokenizer st = new StringTokenizer(lastNmeaMessage, ",");
                stNmea = st.nextToken();
                Log.e("GpsNmea", "stNmea: " + stNmea);

                if (stNmea.equals("$GNRMC")|| stNmea.equals("$GPRMC")) {

                    if (lastNmeaMessage.contains(",A,"))
                    {
                        String[] parts = lastNmeaMessage.split(",");
                        new MyLogger().storeMassage("lastNmeaMessage", "partsA"+parts);
                        pTime = ""+parts[1];
                        pAV = ""+parts[2];
                        pLat = ""+parts[3];
                        pLatD = ""+parts[4];
                        pLong = ""+parts[5];
                        pLongD = ""+parts[6];
                        pSpped = ""+parts[7];
                        pDirDgree = ""+parts[8];
                        pDate = ""+parts[9];
                        new MyLogger().storeMassage("lastNmeaMessage", "parts"+pTime+","+pAV+","+pLat+","+pLong+","+pSpped+"+"+pDirDgree+","+pDate);
                        //new MyLogger().storeMassage("lastNmeaMessage", "valid"+lastNmeaMessage);

                        try {
                            String timeS = ""+pTime;
                            StringTokenizer sts = new StringTokenizer(timeS, ".");
                            Log.e("GpsNmea", "sts: " + sts);
                            time1 = sts.nextToken();
                            Log.e("GpsNmea", "time1 " + time1);
                        }catch (Exception e){
                            time1 = ""+pTime;
                        }

                        try {
                            float f = Float.parseFloat(pSpped);
                            speedFloat = (float) (f * 1.852);
                        } catch (Exception e){
                        }

                        try {
                            String pSp = ""+speedFloat;
                            StringTokenizer sts1 = new StringTokenizer(pSp, ".");
                            String pSp1 = sts1.nextToken();
                            icSpeed = Integer.parseInt(pSp1);
                        } catch (Exception e){
                        }

                        try {
                            latitude = Double.parseDouble(""+pLat);
                            longitude = Double.parseDouble("" + pLong);
                            latitude = revConvertToStandard(latitude);
                            longitude = revConvertToStandard(longitude);
                        } catch (Exception e){
                        }
                    }
                    else {
                        if (lastNmeaMessage.contains(",V,"))
                        {
                            String[] parts = lastNmeaMessage.split(",");
                            //new MyLogger().storeMassage("lastNmeaMessage", "partsV"+parts);
                            pTime = ""+parts[1];
                            pAV = ""+parts[2];
                            pLat = ""+parts[3];
                            pLatD = ""+parts[4];
                            pLong = ""+parts[5];
                            pLongD = ""+parts[6];
                            pSpped = ""+parts[7];
                            pDirDgree = ""+parts[8];
                            pDate = ""+parts[9];

                            //new MyLogger().storeMassage("lastNmeaMessage", "inValid"+lastNmeaMessage);

                            try {
                                String timeS = ""+pTime;
                                StringTokenizer sts = new StringTokenizer(timeS, ".");
                                Log.e("GpsNmea", "sts: " + sts);
                                time1 = sts.nextToken();
                                Log.e("GpsNmea", "time1 " + time1);
                            }catch (Exception e){
                                time1 = ""+pTime;
                            }

                            try {
                                float f = Float.parseFloat(pSpped);
                                speedFloat = (float) (f * 1.852);
                            } catch (Exception e){
                            }

                            try {
                                String pSp = ""+speedFloat;
                                StringTokenizer sts1 = new StringTokenizer(pSp, ".");
                                String pSp1 = sts1.nextToken();
                                icSpeed = Integer.parseInt(pSp1);
                            } catch (Exception e){
                            }

                            try {
                                latitude = Double.parseDouble(""+pLat);
                                longitude = Double.parseDouble("" + pLong);
                                latitude = revConvertToStandard(latitude);
                                longitude = revConvertToStandard(longitude);
                            } catch (Exception e){
                            }
                        }


                    }
                }

                if (stNmea.equals("$GPGGA")|| stNmea.equals("$GNGGA")) {
                    String[] part = lastNmeaMessage.split(",");
                    String GpsFix = part[6];

                    if (GpsFix.equals("0")) {
                        globalVaribale.setGpsAV(false);
                    } else {
                        try {
                            //====> AC
                            if (((icSpeed - ipSpeed) >= aclimit) && (ipSpeed > 5)){
                                String acStamp = "AC," + pDate + "," + time1 + "," +  pLat+ "," + pLatD + "," +pLong + "," + LongD + ","+ DirDegree+"," + icSpeed + ","+ipSpeed+",0.0,A";
                                sqliteDatabase = mContext.openOrCreateDatabase("ac_dc", mContext.MODE_PRIVATE, null);
                                sqliteDatabase.execSQL("CREATE TABLE if not exists AC_DC_stamp(ID INTEGER PRIMARY KEY   AUTOINCREMENT,stamp varchar )");
                                String query2 = "INSERT INTO AC_DC_stamp (stamp)VALUES ( '" + acStamp + "' );";
                                sqliteDatabase.execSQL(query2);
                                sqliteDatabase.close();
                                Log.e("AC", acStamp);
                                new MyLogger().storeMassage("AC stamp", "genrated");
                            }
                        } catch (Exception e){
                            new MyLogger().storeMassage("AC stamp", "Exception:- "+e.getMessage());
                        }
                        try {
                            //====> DC
                            if (((ipSpeed - icSpeed) >= aclimit) && icSpeed > 5){
                                String dcStamp = "DC," + pDate + "," + time1 + "," + pLat + "," + pLatD + "," + pLong + "," + LongD + ","+ DirDegree+"," + icSpeed + ","+ipSpeed+",0.0,A";
                                sqliteDatabase = mContext.openOrCreateDatabase("ac_dc", mContext.MODE_PRIVATE, null);
                                sqliteDatabase.execSQL("CREATE TABLE if not exists AC_DC_stamp(ID INTEGER PRIMARY KEY   AUTOINCREMENT,stamp varchar )");
                                String query2 = "INSERT INTO AC_DC_stamp (stamp)VALUES ( '" + dcStamp + "' );";
                                sqliteDatabase.execSQL(query2);
                                sqliteDatabase.close();
                                Log.e("DC", dcStamp);
                                new MyLogger().storeMassage("DC stamp", "genrated");
                            }
                        } catch (Exception e){
                            new MyLogger().storeMassage("DC stamp", "Exception:- "+e.getMessage());
                        }

                        try {
                            gd.storeData(time1, pAV, pLat, pLatD, pLong, pLongD, "" + icSpeed, pDirDgree, pDate);
                        }catch (Exception e){
                        }

                        try {
                            globalVaribale.setLatitude(Double.parseDouble(pLat));
                            globalVaribale.setLongitude(Double.parseDouble(pLong));
                            globalVaribale.setSpeed(icSpeed);
                            globalVaribale.setDateTime(pDate+","+time1);
                            globalVaribale.setGpsAV(true);

                        } catch (Exception e){
                        }
                        /*String sst = pTime+"\n"+ pAV+"\n"+ pLat+"\n"+ pLatD+"\n"+ pLong+"\n"+ pLongD+"\n"+ pSpped+"\n"+ pDirDgree+"\n"+ pDate;
                        Toast.makeText(mContext, ""+sst, Toast.LENGTH_SHORT).show();*/
                        stampRMC = "stampRMC," + pDate + "," + time1 + "," + latitude + "," + pLatD + "," + longitude + "," + pLongD + ",0.0," + speedFloat + ",0.0,0.0,A";
                        rmc = "RMC," + time1 + ",A," + pLat + "," + pLatD + "," + pLong + "," + pLongD + "," + Speed + "," + pDirDgree + "," + pDate + ",,,A*";
                        globalVaribale.setRmcString(rmc);

                        try {
                            if(icSpeed >= incidentLimit)
                            {
                                File databaseExist3 = mContext.getDatabasePath("incidentData.db");
                                if (databaseExist3.exists()) {
                                    database = mContext.openOrCreateDatabase("incidentData.db", mContext.MODE_PRIVATE, null);
                                    Cursor cr = database.rawQuery("Select *from incidentDataTable", null);
                                    cr.moveToFirst();
                                    while (!cr.isAfterLast())
                                    {
                                        sNo2 = cr.getInt(cr.getColumnIndex("srNo1"));
                                        cr.moveToNext();
                                    }
                                    database.close();
                                    sNo2 = sNo2 + 1;
                                    int count = lastCountI + 1;
                                    if (sNo2 == count){
                                        pI = 1;
                                        File databaseExist21 = mContext.getDatabasePath("pointerI.db");
                                        if (databaseExist21.exists()) {
                                            database = mContext.openOrCreateDatabase("pointerI.db", mContext.MODE_PRIVATE, null);
                                            Cursor cr1 = database.rawQuery("Select *from pointerTableI", null);
                                            cr1.moveToFirst();
                                            while (!cr1.isAfterLast())
                                            {
                                                pI = cr1.getInt(cr1.getColumnIndex("pointI"));
                                                cr1.moveToNext();
                                            }
                                            database.close();
                                        }

                                        if (pI <= lastCountI) {
                                            database = mContext.openOrCreateDatabase("incidentData.db", mContext.MODE_PRIVATE, null);
                                            database.execSQL("update incidentDataTable set incident = '" + rmc + "' where srNo1 = '" + pI + "'");
                                            database.close();
                                            pI = pI + 1;
                                            int count1 = lastCountI + 1;
                                            if (pI == count1){
                                                pI = 1;
                                            }
                                            database = mContext.openOrCreateDatabase("pointerI.db", mContext.MODE_PRIVATE, null);
                                            database.execSQL("create table if not exists pointerTableI (pointI INTEGER(10))");
                                            database.execSQL("insert into pointerTableI(pointI) values('" + pI + "')");
                                            database.close();
                                        }
                                    } else {
                                        database = mContext.openOrCreateDatabase("incidentData.db", mContext.MODE_PRIVATE, null);
                                        database.execSQL("create table if not exists incidentDataTable(srNo1 INTEGER PRIMARY KEY AUTOINCREMENT,incident varchar(150))");
                                        database.execSQL("insert into incidentDataTable(incident) values('" + rmc + "')" + "\n");
                                        database.close();
                                    }
                                } else {
                                    database = mContext.openOrCreateDatabase("incidentData.db", mContext.MODE_PRIVATE, null);
                                    database.execSQL("create table if not exists incidentDataTable(srNo1 INTEGER PRIMARY KEY AUTOINCREMENT,incident varchar(150))");
                                    database.execSQL("insert into incidentDataTable(incident) values('" + rmc + "')" + "\n");
                                    database.close();
                                }

                                new MyLogger().storeMassage("Incident", "Capturing");
                            }
                        } catch (Exception e){
                            new MyLogger().storeMassage("Incident", "Exception:- "+e.getMessage());
                        }


                        if (globalVaribale.isJrmON() == true){
                            try {
                                if (globalVaribale.isOsRed() == true){
                                    mOverSpeedLimit = redZoneOsLimit;
                                } else if (globalVaribale.isOsYellow() == true){
                                    mOverSpeedLimit = yellowZoneOsLimit;
                                } else {
                                    mOverSpeedLimit = greenZoneOsLimit;
                                }
                                pointId = ",$"+globalVaribale.getPointID();
                            } catch (Exception e){
                            }
                        }
                        else
                        {
                            pointId = "";
                        }

                        try {
                            genrateOS(pointId);
                        } catch (Exception e){
                            new MyLogger().storeMassage("OS generate Exception :- ", e.toString());
                        }
                    }
                    Log.e("onNmeaReceived", "Called");
                }

            }
        });
    }

    public void genrateOS(String pid){

        sqLiteDatabase1=mContext.openOrCreateDatabase("SiSt.db",Context.MODE_PRIVATE,null);
        try {
            sqliteDatabase = mContext.openOrCreateDatabase("dist_comp", mContext.MODE_PRIVATE, null);
            String query1 = "select si from stamp;";
            Cursor c = sqliteDatabase.rawQuery(query1, null);
            c.moveToFirst();
            do {
                try {
                    String res = c.getString(0);
                    StringTokenizer st = new StringTokenizer(res, ",");
                    String st1=st.nextToken();
                    String dateString = st.nextToken();
                    timeString = st.nextToken();
                    //convert rmc time to standerd time
                    String time3 = timeconvert(timeString);
                    //convert rmc date to standerd time
                    String date2 = dateconvert(dateString);
                    String dateStart = date2 + " " + time3;
                    //convert current time to standerd time
                    String time5 = timeconvert(time1);
                    //convert current date to satnderd date
                    String date5 = dateconvert(pDate);
                    String dateStop = date5 + " " + time5;
                    //calculate time difference
                    timediff = time_diff(dateStart, dateStop);
                    prev_lat_str = st.nextToken();
                    prev_lat = Double.parseDouble(prev_lat_str);
                    st.nextToken();
                    prev_long_str = st.nextToken();
                    prev_long = Double.parseDouble(prev_long_str);
                    st.nextToken();
                    st.nextToken();
                    String prev_speed_string = st.nextToken();
                    perv_speed = Float.parseFloat(prev_speed_string);
                    curr_ac_limit = (aclimit * (int) timediff);
                    sqliteDatabase.delete("stamp", null, null);
                    if(timediff>0)//to check rmc is not repeated
                    {

                        try {
                            SQLiteDatabase sqLiteDatabase2;
                            sqLiteDatabase2=mContext.openOrCreateDatabase("SiSt.db",Context.MODE_PRIVATE,null);
                            Cursor c5 = sqLiteDatabase1.rawQuery("select * from counter", null);
                            c5.moveToFirst();
                            mCountOS = Integer.parseInt(c5.getString(0));
                            mCountOS1min = Integer.parseInt(c5.getString(1));
                            mOverspeedA = Integer.parseInt(c5.getString(2));
                            mOverspeedB = Integer.parseInt(c5.getString(3));
                            mOverspd = Boolean.parseBoolean(c5.getString(4));
                            mOSpeedFlag = Boolean.parseBoolean(c5.getString(5));
                            sqLiteDatabase2.close();
                        }
                        catch(Exception e)
                        {
                            Log.v("error in reading count",""+e.getMessage());
                        }

                        if (speedFloat > mOverSpeedLimit) {
                            try{
                                mOverspeedA = (int)speedFloat;
                                sqLiteDatabase1.execSQL("update counter set curr_speed=" + mOverspeedA + "");
                                if (mOverspd == false) {
                                    //set flag to show it enter in os stamp
                                    mOverspd = true;
                                    mOverspeedB = mOverspeedA;
                                    sqLiteDatabase1.execSQL("update counter set ospeed='" + true + "'");
                                    sqLiteDatabase1.execSQL("update counter set max_speed='" + mOverspeedB + "'");
                                }
                                Cursor c1 = sqLiteDatabase1.rawQuery("select curr_speed,max_speed from counter", null);
                                c1.moveToFirst();
                                mOverspeedA = Integer.parseInt(c1.getString(0));
                                mOverspeedB = Integer.parseInt(c1.getString(1));

                                if (mOverspeedA > mOverspeedB) {
                                    //update the current speed to max speed
                                    mOverspeedB = mOverspeedA;
                                    sqLiteDatabase1.execSQL("update counter set max_speed='" + mOverspeedB + "'");
                                }

                                Cursor c2 = sqLiteDatabase1.rawQuery("select count,m_counter from counter", null);
                                c2.moveToFirst();
                                mCountOS = Integer.parseInt(c2.getString(0));
                                mCountOS1min = Integer.parseInt(c2.getString(1));

                                if (mCountOS < 10) {

                                    mCountOS++;
                                    mCountOS1min++;
                                    sqLiteDatabase1.execSQL("update counter set count='" + mCountOS + "'");
                                    sqLiteDatabase1.execSQL("update counter set m_counter='" + mCountOS1min + "'");
                                } else {
                                    if (mCountOS >= 10) {

                                        ///JRM Sound Alert for OS
                                        if (osAlert >= 16)
                                        {
                                            if (globalVaribale.isJrmON() == true){

                                                if(mediaPlayer != null && mediaPlayer.isPlaying())
                                                {
                                                    mediaPlayer.stop();
                                                    mediaPlayer.release();
                                                    mediaPlayer = null;
                                                }
                                                else
                                                {
                                                    mediaPlayer.stop();
                                                }
                                                try {
                                                    if (globalVaribale.isOsRed() == true){

                                                        mediaPlayer = MediaPlayer.create(mContext, R.raw.over_speeding_reduce_speed_to_30);
                                                        mediaPlayer.start();

                                                    } else if (globalVaribale.isOsYellow() == true){

                                                        mediaPlayer = MediaPlayer.create(mContext, R.raw.over_speeding_reduce_speed_to_40);
                                                        mediaPlayer.start();

                                                    } else {

                                                        mediaPlayer = MediaPlayer.create(mContext, R.raw.over_speeding_reduce_speed_to_65);
                                                        mediaPlayer.start();

                                                    }
                                                } catch (Exception e){
                                                }
                                            }
                                            osAlert = 0;
                                        }
                                        osAlert++;


                                        //set flag true to generate stamp within 60 seconds
                                        mOSpeedFlag = true;
                                        sqLiteDatabase1.execSQL("update counter set over_speed='" + mOSpeedFlag + "'");
                                        if (mCountOS1min == 60) {

                                            new MyLogger().storeMassage("In Over Speed", "OS Capturing");

                                            try {
                                                File databaseExist53 = mContext.getDatabasePath("osData.db");
                                                if (databaseExist53.exists()) {
                                                    database = mContext.openOrCreateDatabase("osData.db", mContext.MODE_PRIVATE, null);
                                                    Cursor cr2 = database.rawQuery("select * from osDataTable", null);
                                                    cr2.moveToFirst();
                                                    while (!cr2.isAfterLast()) {
                                                        String str77 = cr2.getString(cr2.getColumnIndex("stampA"));
                                                        strB = str77.toString();
                                                        cr2.moveToNext();
                                                    }
                                                    database.close();
                                                    stampOS = "OS," + strB + mOverspeedB + "," + mCountOS + ",0.0,A" + pid;
                                                } else {
                                                    stampOS = "OS," + pDate + "," + time1 + "," + pLat + "," + pLatD + "," + pLong + "," + pLongD + ",0.0," + mOverspeedB + "," + mCountOS + ",0.0,A" + pid;
                                                    String strr = pDate + "," + time1 + "," + pLat + "," + pLatD + "," + pLong + "," + pLongD + ",0.0,";
                                                    database = mContext.openOrCreateDatabase("osData.db", mContext.MODE_PRIVATE, null);
                                                    database.execSQL("create table if not exists osDataTable(stampA varchar(150))");
                                                    database.execSQL("insert into osDataTable(stampA) values('" + strr + "')");
                                                    database.close();
                                                }
                                            }catch (Exception e){
                                            }
                                            //genarte os stamp
                                            //=== stampOS = "OS," + date1 + "," + time1 + "," + getLat + "," + latDir + "," + getLong + "," + longDir + ",0.0," + mOverspeedB + "," + mCountOS + ",0.0,A";

                                            // ==================================> Added by Rakesh on 03/06/16 for new memory managment code

                                            File databaseExist55 = mContext.getDatabasePath("stamps.db");
                                            if (databaseExist55.exists()) {
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
                                                        database.execSQL("update stampsTable set stamps = '" + stampOS + "' where srNo = '" + p + "'");
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
                                                    database.execSQL("insert into stampsTable(stamps) values('" + stampOS + "')" + "\n");
                                                    database.close();
                                                }
                                            } else {
                                                database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                                                database.execSQL("create table if not exists stampsTable(srNo INTEGER PRIMARY KEY AUTOINCREMENT,stamps varchar(150))");
                                                database.execSQL("insert into stampsTable(stamps) values('" + stampOS + "')" + "\n");
                                                database.close();
                                            }

                                            new MyLogger().storeMassage("Over Speed", "OS Captured");
                                            //=================================>
                                            //set mint counter to 0 to start from 0
                                            mCountOS1min = 0;
                                            sqLiteDatabase1.execSQL("update counter set m_counter='" + mCountOS1min + "'");
                                            sqLiteDatabase1.execSQL("create table if not exists SiStTable(stamp varchar)");
                                            sqLiteDatabase1.execSQL("insert into SiStTable values('" + stampOS + "')");
                                        }
                                        //increment the counter and update it database
                                        mCountOS++;
                                        mCountOS1min++;
                                        sqLiteDatabase1.execSQL("update counter set count='" + mCountOS + "'");
                                        sqLiteDatabase1.execSQL("update counter set m_counter='" + mCountOS1min + "'");
                                    }
                                }
                            }
                            catch (Exception e) {
                                Log.v("Error in os",e.getMessage());
                            }
                        } else {

                            ///
                            osAlert = 16;

                            if (mOSpeedFlag == true) {
                                //generate os tsamp when count is less than 60
                                try {
                                    File databaseExist53 = mContext.getDatabasePath("osData.db");
                                    if (databaseExist53.exists()) {
                                        database = mContext.openOrCreateDatabase("osData.db", mContext.MODE_PRIVATE, null);
                                        Cursor cr2 = database.rawQuery("select * from osDataTable", null);
                                        cr2.moveToFirst();
                                        while (!cr2.isAfterLast()) {
                                            String str77 = cr2.getString(cr2.getColumnIndex("stampA"));
                                            strB = str77.toString();
                                            cr2.moveToNext();
                                        }
                                        database.close();
                                        stampOS = "OS," + strB + mOverspeedB + "," + mCountOS + ",0.0,A" + pid;
                                    } else {
                                        stampOS = "OS," + pDate + "," + time1 + "," + pLat + "," + pLatD + "," + pLong + "," + pLongD + ",0.0," + mOverspeedB + "," + mCountOS + ",0.0,A" + pid;
                                    }
                                } catch (Exception e){
                                }

                                // ==================================> Added by Rakesh on 03/06/16 for new memory managment code
                                File databaseExist55 = mContext.getDatabasePath("stamps.db");
                                if (databaseExist55.exists()) {
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
                                            database.execSQL("update stampsTable set stamps = '" + stampOS + "' where srNo = '" + p + "'");
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
                                        database.execSQL("insert into stampsTable(stamps) values('" + stampOS + "')" + "\n");
                                        database.close();
                                    }
                                } else {
                                    database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                                    database.execSQL("create table if not exists stampsTable(srNo INTEGER PRIMARY KEY AUTOINCREMENT,stamps varchar(150))");
                                    database.execSQL("insert into stampsTable(stamps) values('" + stampOS + "')" + "\n");
                                    database.close();
                                }
                                // =================================>
                                try {
                                    sqLiteDatabase1.execSQL("create table if not exists SiStTable(stamp varchar)");
                                    sqLiteDatabase1.execSQL("insert into SiStTable values('" + stampOS + "')");
                                    mOSpeedFlag = false;
                                    mOverspd = false;
                                    mCountOS = 0;
                                    mCountOS1min = 0;
                                    sqLiteDatabase1.execSQL("update counter set count='" + mCountOS + "'");
                                    sqLiteDatabase1.execSQL("update counter set m_counter='" + mCountOS1min + "'");
                                    sqLiteDatabase1.execSQL("update counter set ospeed='" + mOverspd + "'");
                                    sqLiteDatabase1.execSQL("update counter set over_speed='" + mOSpeedFlag + "'");
                                }
                                catch(Exception e) {
                                    Log.e("errr in os",e.getMessage());
                                }

                                new MyLogger().storeMassage("Over Speed", "OS Captured");

                            } else {
                                try {
                                    File databaseExist53 = mContext.getDatabasePath("osData.db");
                                    if (databaseExist53.exists()) {
                                        mContext.deleteDatabase("osData.db");
                                    }
                                } catch (Exception e){
                                }
                                mOSpeedFlag = false;
                                mOverspd = false;
                                mCountOS = 0;
                                mCountOS1min = 0;
                                try {
                                    sqLiteDatabase1.execSQL("update counter set count='" + mCountOS + "'");
                                    sqLiteDatabase1.execSQL("update counter set m_counter='" + mCountOS1min + "'");
                                    sqLiteDatabase1.execSQL("update counter set ospeed='" + mOverspd + "'");
                                    sqLiteDatabase1.execSQL("update counter set over_speed='" + mOSpeedFlag + "'");
                                } catch (Exception e) {
                                    Log.v("error i",e.getMessage());
                                }
                            }
                        }
                    }
                } catch (Exception e)
                {
                    Log.e("Exeption RA RD", ""+e);
                }
            } while (c.moveToNext());
            sqliteDatabase.close();
        } catch (Exception e) {
            Log.v("Error",""+e);
            new MyLogger().storeMassage("Over Speed", "Exception:- " + e.getMessage());
        }

        try {
            sqliteDatabase = mContext.openOrCreateDatabase("dist_comp", mContext.MODE_PRIVATE, null);
            Cursor myres = sqliteDatabase.rawQuery("select * from distance", null);
            myres.moveToFirst();
            float s;
            do {
                String speed1 = myres.getString(0);
                s = Float.parseFloat(speed1);
            } while (myres.moveToNext());

            s = 00;  //==========> By Rakesh
            LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (statusOfGPS == true) {
                if (latitude != 0.0 && longitude != 0.0) {
                    sqliteDatabase.execSQL("CREATE TABLE if not exists stamp(si varchar,timediff int,distance varchar )");
                    String query1 = "INSERT INTO stamp (si,timediff,distance)VALUES ( '" + stampRMC + "'," + timediff + ",'" + s + "' );";
                    sqliteDatabase.execSQL(query1);
                    sqliteDatabase.close();
                }
            }
        } catch(Exception e) {
            Log.v("error in rmc writing", e.getMessage());
        }
        sqliteDatabase = mContext.openOrCreateDatabase("timeDiff.db", mContext.MODE_PRIVATE, null);
        sqliteDatabase.execSQL("Create table if not exists tmStampTable(tmStamp int)");
        String query22 = "INSERT INTO tmStampTable (tmStamp) VALUES('"+time1+"');";
        sqliteDatabase.execSQL(query22);
        sqliteDatabase.close();

        Log.e("OS_Method", "Called");
    }

    private String timeconvert(String t)
    {
        int num=Integer.parseInt(t);
        int ar[]=new int[3];
        int i=0;
        while(num>0)
        {
            ar[i]=num%100;
            num=num/100;
            i++;
        }
        String res=ar[2]+":"+ar[1]+":"+ar[0];
        return res;
    }

    private   String dateconvert(String t)
    {
        int num=Integer.parseInt(t);
        int ar[]=new int[3];
        int i=0;
        while(num>0)
        {
            ar[i]=num%100;
            num=num/100;
            i++;
        }
        String h=String.valueOf(ar[2]);
        String m=String.valueOf(ar[1]);
        String s=String.valueOf(ar[0]);
        if(h.length()==1)
        {
            h="0"+h;
        }
        if(m.length()==1)
        {
            m="0"+m;
        }

        if(s.length()==1)
        {
            s="0"+s;
        }
        String res=h+"/"+m+"/"+s;
        return res;
    }

    private  long time_diff(String dateStart, String dateStop)
    {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        java.util.Date d1 = null;
        Date d2 = null;
        try {
            d1 = format.parse(dateStart);
            d2 = format.parse(dateStop);
            //in milliseconds
            long diff = d2.getTime() - d1.getTime();
            long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffDays = diff / (24 * 60 * 60 * 1000);
            long total_sec=diffSeconds+(diffMinutes*60)+(diffHours*60*60)+(diffDays*24*60*60);
            Log.v("Timediff", "" + total_sec);
            return total_sec;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private double revConvertToStandard(double x){
        float convertedVal;
        double v = x/100;
        long v1 = (long)(v);
        float v2 = (float) (v - v1);
        float v3 = (v2*100) / 60;
        convertedVal = v1 + v3;
        DecimalFormat decimalFormat = new DecimalFormat(".000000");
        double d = Double.parseDouble(decimalFormat.format(convertedVal));
        return d;
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

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
