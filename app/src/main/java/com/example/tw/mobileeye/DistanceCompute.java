package com.example.tw.mobileeye;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by tw on 8/24/2016.
 */
public class DistanceCompute {

    Context mContext;
    SQLiteDatabase database;
    double latitude, longitude, disLat, disLong;
    String date1, time1, strDate, strTime, strLat, strLong, lat, lon;
    float speed, distanceKM, strDisF;
    int tmD, dtD, cTM, pTM, cDT, pDT;
    String AV, Speed;


    public DistanceCompute(Context mContext) {
        this.mContext = mContext;
    }

    public void getDistance(){

        new MyLogger().storeMassage("DistanceCompute", "getDistance method Called");

        try {
            File databaseExistD = mContext.getDatabasePath("GprmcDB");
            if (databaseExistD.exists())
            {
                GprmcDatabase gd = new GprmcDatabase(mContext);
                time1 = gd.getTime();
                AV = gd.getAV();
                lat = gd.getLatitude();
                lon = gd.getLongitude();
                Speed = gd.getSpeed();
                date1 = gd.getDate();
            }

            latitude = Double.parseDouble(""+lat);
            longitude = Double.parseDouble(""+lon);
            latitude = revConvertToStandard(latitude);
            longitude = revConvertToStandard(longitude);
            speed = Float.parseFloat(Speed);

            //String str = "lat "+latitude+"\n lon "+longitude+"\n speed "+speed;
            //Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();


            File databaseExist16 = mContext.getDatabasePath("DistComp2.db");
            if (databaseExist16.exists()) {
                database = mContext.openOrCreateDatabase("DistComp2.db", mContext.MODE_PRIVATE, null);
                Cursor cr = database.rawQuery("Select * from DistCompTable2", null);
                cr.moveToFirst();
                while (!cr.isAfterLast())
                {
                    strLat = cr.getString(cr.getColumnIndex("DisLat2"));
                    strLong = cr.getString(cr.getColumnIndex("DisLong2"));
                    strDate = cr.getString(cr.getColumnIndex("DisDate2"));
                    strTime = cr.getString(cr.getColumnIndex("DisTime2"));
                    strDisF = cr.getFloat(cr.getColumnIndex("Distance2"));
                    cr.moveToNext();
                }

                try {
                    disLat = Double.parseDouble(strLat);
                    disLong = Double.parseDouble(strLong);
                    cDT = Integer.valueOf(date1);
                    cTM = Integer.valueOf(time1);
                    pDT = Integer.valueOf(strDate);
                    pTM = Integer.valueOf(strTime);
                    tmD = cTM - pTM;
                    dtD = cDT - pDT;
                } catch (Exception e){
                }

                if(speed > 0 && ((latitude != 0.0 && disLat != 0.0) && (longitude != 0.0 && disLong != 0.0)))
                {
                    try {
                        Location srcLocation2 = new Location("");
                        Location destLocation2 = new Location("");
                        destLocation2.setLatitude(latitude);
                        destLocation2.setLongitude(longitude);
                        srcLocation2.setLatitude(disLat);
                        srcLocation2.setLongitude(disLong);
                        double distance2 = srcLocation2.distanceTo(destLocation2);//in meters
                        distance2 = distance2 / 1000;// convert it to kilometer
                        //double distance3 = Double.parseDouble(String.valueOf(strDisF));
                        double distance4 = distance2 + strDisF;
                        distanceKM = (float) distance4;
                        Log.e("MyReciver", "Start DistanceCompute_SP");
                    } catch (Exception e)
                    {
                    }
                    database = mContext.openOrCreateDatabase("DistComp2.db", mContext.MODE_PRIVATE, null);
                    database.execSQL("update DistCompTable2 set Distance2 = '" + distanceKM + "'");
                    database.execSQL("update DistCompTable2 set DisLat2 = '" + latitude + "'");
                    database.execSQL("update DistCompTable2 set DisLong2 = '" + longitude + "'");
                    database.close();
                } else
                {
                    if ((tmD > 500 || dtD != 0) && ((latitude != 0.0 && disLat != 0.0) && (longitude != 0.0 && disLong != 0.0)))
                    {
                        try {
                            Location srcLocation2 = new Location("");
                            Location destLocation2 = new Location("");
                            destLocation2.setLatitude(latitude);
                            destLocation2.setLongitude(longitude);
                            srcLocation2.setLatitude(disLat);
                            srcLocation2.setLongitude(disLong);
                            double distance2 = srcLocation2.distanceTo(destLocation2);//in meters
                            distance2 = distance2 / 1000;// convert it to kilometer
                            //double distance3 = Double.parseDouble(String.valueOf(strDisF));
                            double distance4 = distance2 + strDisF;
                            distanceKM = (float) distance4;
                            Log.e("MyReciver", "Start DistanceCompute_TD");
                        } catch (Exception e)
                        {
                        }

                        database = mContext.openOrCreateDatabase("DistComp2.db", mContext.MODE_PRIVATE, null);
                        database.execSQL("update DistCompTable2 set Distance2 = '" + distanceKM + "'");
                        database.execSQL("update DistCompTable2 set DisLat2 = '" + latitude + "'");
                        database.execSQL("update DistCompTable2 set DisLong2 = '" + longitude + "'");
                        database.close();
                    }
                }

                if (distanceKM > 0){
                } else {
                    database = mContext.openOrCreateDatabase("DistComp2.db", mContext.MODE_PRIVATE, null);
                    database.execSQL("update DistCompTable2 set DisLat2 = '" + latitude + "'");
                    database.execSQL("update DistCompTable2 set DisLong2 = '" + longitude + "'");
                    database.close();
                }

                database = mContext.openOrCreateDatabase("DistComp2.db", mContext.MODE_PRIVATE, null);
                database.execSQL("update DistCompTable2 set DisDate2 = '" + date1 + "'");
                database.execSQL("update DistCompTable2 set DisTime2 = '" + time1 + "'");
                database.close();
            } else {
                distanceKM = 00;
                database = mContext.openOrCreateDatabase("DistComp2.db", mContext.MODE_PRIVATE, null);
                database.execSQL("Create table if not exists DistCompTable2(DisLat2 VARCHAR(20), DisLong2 VARCHAR(20), Distance2 float(20), DisTime2 VARCHAR(20), DisDate2 VARCHAR(20))");
                database.execSQL("insert into DistCompTable2(DisLat2) values('" + latitude + "')");
                database.execSQL("insert into DistCompTable2(DisLong2) values('" + longitude + "')");
                database.execSQL("insert into DistCompTable2(Distance2) values('" + distanceKM +"')");
                database.execSQL("insert into DistCompTable2(DisDate2) values('" + date1 + "')");
                database.execSQL("insert into DistCompTable2(DisTime2) values('" + time1 + "')");
                database.close();
            }

        } catch (Exception e){
            new MyLogger().storeMassage("DistanceCompute", "getDistance Exception:- "+e );
        }
        //Log.e("MyReciver", "DistanceCompute Called "+distanceKM+", "+date1+ time1+ strDate+ strTime+ strLat+ strLong+ strDisF+tmD+ dtD+ cTM+ pTM+ cDT+ pDT);
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

    private  long r_time_diff(String dateStart, String dateStop)
    {
        SimpleDateFormat format = new SimpleDateFormat("ddMMyy HHmmss");
        java.util.Date d1 = null;
        Date d2 = null;
        try {
            d1 = format.parse(dateStart);
            d2 = format.parse(dateStop);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long diff = d2.getTime() - d1.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);
        long total_sec=diffSeconds+(diffMinutes*60)+(diffHours*60*60)+(diffDays*24*60*60);
        Log.v("r_Timediff", "" + total_sec);

        return total_sec;
    }
}
