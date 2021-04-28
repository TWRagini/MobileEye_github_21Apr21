package com.example.tw.mobileeye;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * Created by tw on 9/8/2016.
 */
public class GprmcDatabase {

    Context mContext;
    SQLiteDatabase database;

    public GprmcDatabase(Context mContext) {
        this.mContext = mContext;
    }

    public void storeData(String pTime,String pAV,String pLat,String pLatD,String pLong,String pLongD,String pSpped,String pDirDgree,String pDate)
    {

        File databaseExist = mContext.getDatabasePath("GprmcDB");
        if (databaseExist.exists())
        {
            database = mContext.openOrCreateDatabase("GprmcDB", mContext.MODE_PRIVATE, null);
            database.execSQL("update GprmcDBTable set nTime = '" + pTime + "'");
            database.execSQL("update GprmcDBTable set nAV = '" + pAV + "'");
            database.execSQL("update GprmcDBTable set nLat = '" + pLat + "'");
            database.execSQL("update GprmcDBTable set nLatD = '" + pLatD + "'");
            database.execSQL("update GprmcDBTable set nLong = '" + pLong + "'");
            database.execSQL("update GprmcDBTable set nLongD = '" + pLongD + "'");
            database.execSQL("update GprmcDBTable set nSpped = '" + pSpped + "'");
            database.execSQL("update GprmcDBTable set nDirDgree = '" + pDirDgree + "'");
            database.execSQL("update GprmcDBTable set nDate = '" + pDate + "'");
            database.close();
        }
        else
        {
            database = mContext.openOrCreateDatabase("GprmcDB", mContext.MODE_PRIVATE, null);
            database.execSQL("Create table if not exists GprmcDBTable(nTime VARCHAR(20), nAV VARCHAR(20), nLat VARCHAR(20), nLatD INTEGER(20), nLong VARCHAR(20), nLongD VARCHAR(20), nSpped INTEGER(20), nDirDgree VARCHAR(20), nDate VARCHAR(20))");
            database.execSQL("insert into GprmcDBTable(nTime) values('" + pTime + "')");
            database.execSQL("insert into GprmcDBTable(nAV) values('" + pAV + "')");
            database.execSQL("insert into GprmcDBTable(nLat) values('" + pLat + "')");
            database.execSQL("insert into GprmcDBTable(nLatD) values('" + pLatD + "')");
            database.execSQL("insert into GprmcDBTable(nLong) values('" + pLong + "')");
            database.execSQL("insert into GprmcDBTable(nLongD) values('" + pLongD + "')");
            database.execSQL("insert into GprmcDBTable(nSpped) values('" + pSpped + "')");
            database.execSQL("insert into GprmcDBTable(nDirDgree) values('" + pDirDgree + "')");
            database.execSQL("insert into GprmcDBTable(nDate) values('" + pDate + "')");
            database.close();
        }
    }

    public String getTime(){

        String time = "";
        database = mContext.openOrCreateDatabase("GprmcDB", mContext.MODE_PRIVATE, null);
        Cursor cr = database.rawQuery("Select * from GprmcDBTable", null);
        cr.moveToFirst();
        while (!cr.isAfterLast())
        {
            time = cr.getString(cr.getColumnIndex("nTime"));
            cr.moveToNext();
        }
        database.close();
        return time;
    }

    public String getAV(){

        String AV = "";
        database = mContext.openOrCreateDatabase("GprmcDB", mContext.MODE_PRIVATE, null);
        Cursor cr = database.rawQuery("Select * from GprmcDBTable", null);
        cr.moveToFirst();
        while (!cr.isAfterLast())
        {
            AV = cr.getString(cr.getColumnIndex("nAV"));
            cr.moveToNext();
        }
        database.close();
        return AV;
    }

    public String getLatitude(){

        String Lat = "";
        database = mContext.openOrCreateDatabase("GprmcDB", mContext.MODE_PRIVATE, null);
        Cursor cr = database.rawQuery("Select * from GprmcDBTable", null);
        cr.moveToFirst();
        while (!cr.isAfterLast())
        {
            Lat = cr.getString(cr.getColumnIndex("nLat"));
            cr.moveToNext();
        }
        database.close();
        return Lat;
    }

    public String getLatDir(){

        String LatD = "";
        database = mContext.openOrCreateDatabase("GprmcDB", mContext.MODE_PRIVATE, null);
        Cursor cr = database.rawQuery("Select * from GprmcDBTable", null);
        cr.moveToFirst();
        while (!cr.isAfterLast())
        {
            LatD = cr.getString(cr.getColumnIndex("nLatD"));
            cr.moveToNext();
        }
        database.close();
        return LatD;
    }

    public String getLongitude(){

        String Lon = "";
        database = mContext.openOrCreateDatabase("GprmcDB", mContext.MODE_PRIVATE, null);
        Cursor cr = database.rawQuery("Select * from GprmcDBTable", null);
        cr.moveToFirst();
        while (!cr.isAfterLast())
        {
            Lon = cr.getString(cr.getColumnIndex("nLong"));
            cr.moveToNext();
        }
        database.close();
        return Lon;
    }

    public String getLongDir(){

        String LongD = "";
        database = mContext.openOrCreateDatabase("GprmcDB", mContext.MODE_PRIVATE, null);
        Cursor cr = database.rawQuery("Select * from GprmcDBTable", null);
        cr.moveToFirst();
        while (!cr.isAfterLast())
        {
            LongD = cr.getString(cr.getColumnIndex("nLongD"));
            cr.moveToNext();
        }
        database.close();
        return LongD;
    }

    public String getSpeed(){

        String Speed = "";
        database = mContext.openOrCreateDatabase("GprmcDB", mContext.MODE_PRIVATE, null);
        Cursor cr = database.rawQuery("Select * from GprmcDBTable", null);
        cr.moveToFirst();
        while (!cr.isAfterLast())
        {
            Speed = cr.getString(cr.getColumnIndex("nSpped"));
            cr.moveToNext();
        }
        database.close();
        return Speed;
    }

    public String getDirDegree(){

        String DirDegree = "";
        database = mContext.openOrCreateDatabase("GprmcDB", mContext.MODE_PRIVATE, null);
        Cursor cr = database.rawQuery("Select * from GprmcDBTable", null);
        cr.moveToFirst();
        while (!cr.isAfterLast())
        {
            DirDegree = cr.getString(cr.getColumnIndex("nDirDgree"));
            cr.moveToNext();
        }
        database.close();
        return DirDegree;
    }

    public String getDate(){

        String Date = "";
        database = mContext.openOrCreateDatabase("GprmcDB", mContext.MODE_PRIVATE, null);
        Cursor cr = database.rawQuery("Select * from GprmcDBTable", null);
        cr.moveToFirst();
        while (!cr.isAfterLast())
        {
            Date = cr.getString(cr.getColumnIndex("nDate"));
            cr.moveToNext();
        }
        database.close();
        return Date;
    }
}
