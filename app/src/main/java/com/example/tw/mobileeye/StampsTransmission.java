package com.example.tw.mobileeye;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

import com.example.tw.mobileeye.GlobalVariable;
import com.example.tw.mobileeye.MyLogger;
import com.example.tw.mobileeye.TWsimpleMailSender;

import java.io.File;

public class StampsTransmission extends Service {
    Context mContext;
    SQLiteDatabase database, sqliteDatabase, databaseP1, databaseP, databasec, databaseS;
    String str1, str4, data, stamp, unitID, smtpPassword, smtpHost, smtpport, toMail;
    TelephonyManager telMgr;
    int simState;
    private int sNo1;
    int p;
    int lastCount;
    String str21, pointerSI;
    int pRead, pWrite;
    StringBuffer sb, sb11, sb22;
    int mailSize;
    int pWrite1 = 0;
    GlobalVariable globalVariable;

    @Override
    public void onStart(Intent intent, int startId) {
        mContext = this;
        getMethod();
        super.onStart(intent, startId);
    }

    public void getMethod(){
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        new MyLogger().storeMassage("StampsTransmission", "Start Stamps Transmission");

        databasec = mContext.openOrCreateDatabase("Config", mContext.MODE_PRIVATE, null);
        databasec.execSQL("create table if not exists file(id INTEGER PRIMARY KEY AUTOINCREMENT,parameter varchar(100) unique,value varchar(150))");
        try {

            unitID = retrive("unitId");
            smtpPassword = retrive("smtpPW");
            smtpHost = retrive("smtpHost");
            smtpport = retrive("smtpPort");
            toMail = retrive("toMail");
            lastCount = retriveInt("STMdbSize");
            mailSize = retriveInt("STMmailSize");

        }catch (Exception e){
        }
        databasec.close();

        File databaseExist16 = mContext.getDatabasePath("lastData.db");
        if (databaseExist16.exists()) {

            database = mContext.openOrCreateDatabase("lastData.db", mContext.MODE_PRIVATE, null);
            Cursor cr7 = database.rawQuery("Select * from lastDataTable", null);
            cr7.moveToFirst();
            while (!cr7.isAfterLast())
            {
                data = cr7.getString(cr7.getColumnIndex("lastData"));
                cr7.moveToNext();
            }

            stamp = data.toString();
            database.close();
            telMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            simState = telMgr.getSimState();

            if ((simState == TelephonyManager.SIM_STATE_ABSENT) || !(simState == TelephonyManager.SIM_STATE_READY)) {

                File databaseExist8 = mContext.getDatabasePath("stamps.db");
                if (databaseExist8.exists()) {
                    database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                    Cursor cr = database.rawQuery("Select *from stampsTable", null);
                     // String s = cr.getString(cr.getColumnIndex("lastData"));
                   // Log.e("lastData2", "getMethod: " + s);
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
                        File databaseExist = mContext.getDatabasePath("pointer.db");
                        if (databaseExist.exists()) {
                            databaseP = mContext.openOrCreateDatabase("pointer.db", mContext.MODE_PRIVATE, null);
                            Cursor cr1 = databaseP.rawQuery("Select *from pointerTable", null);
                            cr1.moveToFirst();
                            while (!cr1.isAfterLast())
                            {
                                p = cr1.getInt(cr1.getColumnIndex("point"));
                                cr1.moveToNext();
                            }
                            databaseP.close();
                        }

                        if (p <= lastCount) {
                            database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                            database.execSQL("update stampsTable set stamps = '" + "NGSM,"+stamp + "' where srNo = '" + p + "'");
                            database.close();
                            p = p + 1;
                            int count1 = lastCount + 1;
                            if (p == count1){
                                p = 1;
                            }

                            databaseP = mContext.openOrCreateDatabase("pointer.db", mContext.MODE_PRIVATE, null);
                            databaseP.execSQL("create table if not exists pointerTable (point INTEGER(10))");
                            databaseP.execSQL("insert into pointerTable(point) values('" + p + "')");
                            databaseP.close();
                        }
                    } else {
                        database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                        database.execSQL("create table if not exists stampsTable(srNo INTEGER PRIMARY KEY AUTOINCREMENT,stamps varchar(150))");
                        database.execSQL("insert into stampsTable(stamps) values('" + "NGSM,"+stamp + "')" + "\n");
                        database.close();
                    }
                } else {
                    database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                    database.execSQL("create table if not exists stampsTable(srNo INTEGER PRIMARY KEY AUTOINCREMENT,stamps varchar(150))");
                    database.execSQL("insert into stampsTable(stamps) values('" + "NGSM,"+stamp + "')" + "\n");
                    database.close();
                }
            }
        }

        ConnectivityManager conMgr = ( ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        {
            NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
            if (netInfo == null) {

                File databaseExist7 = mContext.getDatabasePath("stamps.db");
                if (databaseExist7.exists()) {
                    database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
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
                        File databaseExist = mContext.getDatabasePath("pointer.db");
                        if (databaseExist.exists()) {
                            databaseP = mContext.openOrCreateDatabase("pointer.db", mContext.MODE_PRIVATE, null);
                            Cursor cr1 = databaseP.rawQuery("Select *from pointerTable", null);
                            cr1.moveToFirst();
                            while (!cr1.isAfterLast())
                            {
                                p = cr1.getInt(cr1.getColumnIndex("point"));
                                cr1.moveToNext();
                            }
                            databaseP.close();
                        }

                        if (p <= lastCount) {
                            database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                            database.execSQL("update stampsTable set stamps = '" + "NGPRS,"+stamp + "' where srNo = '" + p + "'");
                            database.close();
                            p = p + 1;
                            int count1 = lastCount + 1;
                            if (p == count1){
                                p = 1;
                            }
                            databaseP = mContext.openOrCreateDatabase("pointer.db", mContext.MODE_PRIVATE, null);
                            databaseP.execSQL("create table if not exists pointerTable (point INTEGER(10))");
                            databaseP.execSQL("insert into pointerTable(point) values('" + p + "')");
                            databaseP.close();
                        }
                    } else {
                        database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                        database.execSQL("create table if not exists stampsTable(srNo INTEGER PRIMARY KEY AUTOINCREMENT,stamps varchar(150))");
                        database.execSQL("insert into stampsTable(stamps) values('" + "NGPRS,"+stamp + "')" + "\n");
                        database.close();
                    }
                } else {
                    database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                    database.execSQL("create table if not exists stampsTable(srNo INTEGER PRIMARY KEY AUTOINCREMENT,stamps varchar(150))");
                    database.execSQL("insert into stampsTable(stamps) values('" + "NGPRS,"+stamp + "')" + "\n");
                    database.close();
                }
            } else {
                try {
                    TWsimpleMailSender sender = new TWsimpleMailSender(unitID, smtpPassword, smtpHost, smtpport);
                    //sender.sendMail("23", "jhn", "415", "jskkjg@jsg.com"); // this is only for
                    new TestSendMail().execute(sender);

                } catch (Exception e) {
                    Log.e("StampsTransmission", "Exception:- " + e.getMessage());
                    new MyLogger().storeMassage("StampsTransmission", "Exception:- " + e.getMessage());
                    e.printStackTrace();
                    //==================> Added on 11-July-16 by Rakesh
                }
            }
        }
        Log.e("MyReciverSIST", "StampsTransmission Called");
    }

    private String retrive(String p) {
        try {
            String res;
            Cursor c = databasec.rawQuery("select value from file where parameter='" + p + "'", null);
            c.moveToFirst();
            do {
                res = c.getString(0);
            } while (c.moveToNext());
            return res;
        } catch (Exception e) {
            return "error" + e.getMessage();
        }
    }

    private Integer retriveInt(String p) {
        try {
            int res;
            Cursor c = databasec.rawQuery("select value from file where parameter='" + p + "'", null);
            c.moveToFirst();
            do {
                res = c.getInt(0);
            } while (c.moveToNext());
            return res;
        } catch (Exception e) {
        }
        return null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class SendMail extends AsyncTask
    {
        @Override
        protected Object doInBackground(Object[] objects) {
            TWsimpleMailSender sender = (TWsimpleMailSender) objects[0];
            try {
                sender.sendMail(unitID, str1, unitID, toMail);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class TestSendMail extends AsyncTask
    {
        @Override
        protected Object doInBackground(Object[] objects) {

            TWsimpleMailSender sender = (TWsimpleMailSender) objects[0];
            try {
                sender.sendMail("23", "jhn", "415", "jskkjg@jsg.com");

                File databaseExist = mContext.getDatabasePath("ac_dc");
                if (!databaseExist.exists()) {
                    Log.i("Database", "Not Found");
                } else {
                    /*TextView tv1 = new TextView(mContext);
                    tv1.setId(0);*/
                    StringBuffer sb = new StringBuffer();
                    sqliteDatabase = mContext.openOrCreateDatabase("ac_dc", mContext.MODE_PRIVATE, null);
                    sqliteDatabase.execSQL("CREATE TABLE if not exists AC_DC_stamp(ID INTEGER PRIMARY KEY   AUTOINCREMENT,stamp varchar )");
                    String str2 = "Select * from AC_DC_stamp";
                    Cursor cr2 = sqliteDatabase.rawQuery(str2, null);
                    cr2.moveToFirst();
                    while (!cr2.isAfterLast()) {
                        String str3 = cr2.getString(cr2.getColumnIndex("stamp"));
                        //tv1.append("" + str3.toString() + "\n");
                        sb.append("" + str3.toString() + "\n");
                        cr2.moveToNext();
                        str4 = sb.toString();//tv1.getText().toString();
                    }
                    sqliteDatabase.close();

                    File databaseExist9 = mContext.getDatabasePath("stamps.db");
                    if (databaseExist9.exists()) {
                        database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
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
                            File databaseExist0 = mContext.getDatabasePath("pointer.db");
                            if (databaseExist0.exists()) {
                                databaseP = mContext.openOrCreateDatabase("pointer.db", mContext.MODE_PRIVATE, null);
                                Cursor cr1 = databaseP.rawQuery("Select *from pointerTable", null);
                                cr1.moveToFirst();
                                while (!cr1.isAfterLast())
                                {
                                    p = cr1.getInt(cr1.getColumnIndex("point"));
                                    cr1.moveToNext();
                                }
                                databaseP.close();
                            }

                            if (p <= lastCount) {
                                database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                                database.execSQL("update stampsTable set stamps = '" + str4 + "' where srNo = '" + p + "'");
                                database.close();
                                p = p + 1;
                                int count1 = lastCount + 1;
                                if (p == count1){
                                    p = 1;
                                }
                                databaseP = mContext.openOrCreateDatabase("pointer.db", mContext.MODE_PRIVATE, null);
                                databaseP.execSQL("create table if not exists pointerTable (point INTEGER(10))");
                                databaseP.execSQL("insert into pointerTable(point) values('" + p + "')");
                                databaseP.close();
                            }
                        } else {
                            database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                            database.execSQL("create table if not exists stampsTable(srNo INTEGER PRIMARY KEY AUTOINCREMENT,stamps varchar(150))");
                            database.execSQL("insert into stampsTable(stamps) values('" + str4 + "')" + "\n");
                            database.close();
                        }
                    } else {
                        database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                        database.execSQL("create table if not exists stampsTable(srNo INTEGER PRIMARY KEY AUTOINCREMENT,stamps varchar(150))");
                        database.execSQL("insert into stampsTable(stamps) values('" + str4 + "')" + "\n");
                        database.close();
                    }
                    mContext.deleteDatabase("ac_dc");
                }
                Log.v("unitIDSIST", "" + unitID);
                Log.v("PasswordSIST", "" + smtpPassword);
                Log.d("SmtpToMailSIST", "" + toMail);

                File databaseExist5 = mContext.getDatabasePath("stamps.db");
                if (databaseExist5.exists()) {

                    try {

                        /*TextView tv = new TextView(mContext.getApplicationContext());
                        tv.setId(0);*/
                        //StringBuffer sb2 = new StringBuffer();

                        database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                        Cursor cr = database.rawQuery("Select * from stampsTable", null);
                        cr.moveToFirst();
                        while (!cr.isAfterLast()) {
                            sNo1 = cr.getInt(cr.getColumnIndex("srNo"));
                            cr.moveToNext();
                        }
                        database.close();

                        //========================================>
                        File databaseExist0 = mContext.getDatabasePath("pointer.db");
                        if (databaseExist0.exists()) {
                            databaseP = mContext.openOrCreateDatabase("pointer.db", mContext.MODE_PRIVATE, null);
                            Cursor cr0 = databaseP.rawQuery("Select *from pointerTable", null);
                            cr0.moveToFirst();
                            while (!cr0.isAfterLast()) {
                                pWrite = cr0.getInt(cr0.getColumnIndex("point"));
                                cr0.moveToNext();
                            }
                            databaseP.close();
                        }

                        if (sNo1 == lastCount) {
                            pWrite = pWrite - 1;
                        } else {
                            pWrite = sNo1;
                        }
                        pRead = 1;
                        File databaseExist6 = mContext.getDatabasePath("pointer1.db");
                        if (databaseExist6.exists()) {
                            databaseP1 = mContext.openOrCreateDatabase("pointer1.db", mContext.MODE_PRIVATE, null);
                            Cursor cr0 = databaseP1.rawQuery("Select *from pointerTable1", null);
                            cr0.moveToFirst();
                            while (!cr0.isAfterLast()) {
                                pRead = cr0.getInt(cr0.getColumnIndex("point1"));
                                cr0.moveToNext();
                            }
                            databaseP1.close();
                        }

                        //=================> For check repeat pointer

                        File databaseExist64 = mContext.getDatabasePath("checkPointer.db");
                        if (databaseExist64.exists()) {

                            database = mContext.openOrCreateDatabase("checkPointer.db", mContext.MODE_PRIVATE, null);
                            database.execSQL("create table if not exists checkPointerTable (checkPointer1 INTEGER(10))");

                            Cursor cr0 = database.rawQuery("Select *from checkPointerTable", null);
                            cr0.moveToFirst();
                            while (!cr0.isAfterLast()) {
                                pWrite1 = cr0.getInt(cr0.getColumnIndex("checkPointer1"));
                                cr0.moveToNext();
                            }

                            if (pWrite1 == pWrite) {
                                pWrite = pWrite + 1;
                                //pRead =  pWrite - 1;
                            }

                            database.execSQL("update checkPointerTable set checkPointer1 = '" + pWrite + "'");
                            database.close();
                        } else {
                            database = mContext.openOrCreateDatabase("checkPointer.db", mContext.MODE_PRIVATE, null);
                            database.execSQL("create table if not exists checkPointerTable (checkPointer1 INTEGER(10))");
                            database.execSQL("insert into checkPointerTable(checkPointer1) values('" + pWrite + "')");
                            database.close();
                        }

                        pointerSI = "Write Pointer = " + pWrite + ", Read Pointer = " + pRead + ", Write Pointer2 = " + pWrite1;
                        int di = pRead - pWrite;
                        if (di <= mailSize && di >= 1) {
                            pRead = pWrite - 10;
                        }

                        if (pRead == pWrite)
                        {
                        } else
                        {
                            if (pRead > pWrite) {
                                int d = lastCount - pRead;
                                if (d < mailSize) {
                                    database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                                    Cursor cr2 = database.rawQuery("select * from stampsTable where srNo >= '" + pRead + "' and srNo <= '" + lastCount + "'", null);
                                    sb11 = new StringBuffer();
                                    cr2.moveToFirst();
                                    while (!cr2.isAfterLast()) {
                                        String str5 = cr2.getString(cr2.getColumnIndex("stamps"));
                                        sb11.append("" + str5.toString() + "\n");
                                        cr2.moveToNext();
                                    }

                                    Cursor cr3 = database.rawQuery("select * from stampsTable where srNo = '" + pWrite + "'", null);
                                    sb22 = new StringBuffer();
                                    cr3.moveToFirst();
                                    while (!cr3.isAfterLast()) {
                                        String str5 = cr3.getString(cr3.getColumnIndex("stamps"));
                                        sb22.append("" + str5.toString() + "\n");
                                        cr3.moveToNext();
                                    }
                                    sb = new StringBuffer();
                                    sb.append("" + sb11 + sb22 + "\n");
                                    database.close();
                                    int c = 1;
                                    databaseP1 = mContext.openOrCreateDatabase("pointer1.db", mContext.MODE_PRIVATE, null);
                                    databaseP1.execSQL("create table if not exists pointerTable1 (point1 INTEGER(10))");
                                    databaseP1.execSQL("insert into pointerTable1(point1) values('" + c + "')");
                                    databaseP1.close();
                                } else {
                                    int l = pRead + mailSize;
                                    database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                                    Cursor cr2 = database.rawQuery("select * from stampsTable where srNo >= '" + pRead + "' and srNo <= '" + l + "'", null);
                                    sb11 = new StringBuffer();
                                    cr2.moveToFirst();
                                    while (!cr2.isAfterLast()) {
                                        String str5 = cr2.getString(cr2.getColumnIndex("stamps"));
                                        sb11.append("" + str5.toString() + "\n");
                                        cr2.moveToNext();
                                    }
                                    Cursor cr3 = database.rawQuery("select * from stampsTable where srNo = '" + pWrite + "'", null);
                                    sb22 = new StringBuffer();
                                    cr3.moveToFirst();
                                    while (!cr3.isAfterLast()) {
                                        String str5 = cr3.getString(cr3.getColumnIndex("stamps"));
                                        sb22.append("" + str5.toString() + "\n");
                                        cr3.moveToNext();
                                    }
                                    sb = new StringBuffer();
                                    sb.append("" + sb11 + sb22 + "\n");
                                    database.close();

                                    int c = l + 1;
                                    databaseP1 = mContext.openOrCreateDatabase("pointer1.db", mContext.MODE_PRIVATE, null);
                                    databaseP1.execSQL("create table if not exists pointerTable1 (point1 INTEGER(10))");
                                    databaseP1.execSQL("insert into pointerTable1(point1) values('" + c + "')");
                                    databaseP1.close();
                                }
                            } else {
                                int size = pWrite - pRead;
                                if (size > mailSize) {
                                    int d = lastCount - pRead;
                                    if (d < mailSize) {
                                        database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                                        Cursor cr2 = database.rawQuery("select * from stampsTable where srNo >= '" + pRead + "' and srNo <= '" + lastCount + "'", null);
                                        sb11 = new StringBuffer();
                                        cr2.moveToFirst();
                                        while (!cr2.isAfterLast()) {
                                            String str5 = cr2.getString(cr2.getColumnIndex("stamps"));
                                            sb11.append("" + str5.toString() + "\n");
                                            cr2.moveToNext();
                                        }
                                        Cursor cr3 = database.rawQuery("select * from stampsTable where srNo = '" + pWrite + "'", null);
                                        sb22 = new StringBuffer();
                                        cr3.moveToFirst();
                                        while (!cr3.isAfterLast()) {
                                            String str5 = cr3.getString(cr3.getColumnIndex("stamps"));
                                            sb22.append("" + str5.toString() + "\n");
                                            cr3.moveToNext();
                                        }
                                        sb = new StringBuffer();
                                        sb.append("" + sb11 + sb22 + "\n");
                                        database.close();

                                        int c = 1;
                                        databaseP1 = mContext.openOrCreateDatabase("pointer1.db", mContext.MODE_PRIVATE, null);
                                        databaseP1.execSQL("create table if not exists pointerTable1 (point1 INTEGER(10))");
                                        databaseP1.execSQL("insert into pointerTable1(point1) values('" + c + "')");
                                        databaseP1.close();
                                    } else {
                                        int l = pRead + mailSize;
                                        database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                                        Cursor cr2 = database.rawQuery("select * from stampsTable where srNo >= '" + pRead + "' and srNo <= '" + l + "'", null);
                                        sb11 = new StringBuffer();
                                        cr2.moveToFirst();
                                        while (!cr2.isAfterLast()) {
                                            String str5 = cr2.getString(cr2.getColumnIndex("stamps"));
                                            sb11.append("" + str5.toString() + "\n");
                                            cr2.moveToNext();
                                        }
                                        Cursor cr3 = database.rawQuery("select * from stampsTable where srNo = '" + pWrite + "'", null);
                                        sb22 = new StringBuffer();
                                        cr3.moveToFirst();
                                        while (!cr3.isAfterLast()) {
                                            String str5 = cr3.getString(cr3.getColumnIndex("stamps"));
                                            sb22.append("" + str5.toString() + "\n");
                                            cr3.moveToNext();
                                        }
                                        sb = new StringBuffer();
                                        sb.append("" + sb11 + sb22 + "\n");
                                        database.close();

                                        int c = l + 1;
                                        databaseP1 = mContext.openOrCreateDatabase("pointer1.db", mContext.MODE_PRIVATE, null);
                                        databaseP1.execSQL("create table if not exists pointerTable1 (point1 INTEGER(10))");
                                        databaseP1.execSQL("insert into pointerTable1(point1) values('" + c + "')");
                                        databaseP1.close();
                                    }
                                } else {
                                    database = mContext.openOrCreateDatabase("stamps.db", mContext.MODE_PRIVATE, null);
                                    Cursor cr2 = database.rawQuery("select * from stampsTable where srNo >= '" + pRead + "' and srNo <= '" + pWrite + "'", null);
                                    sb = new StringBuffer();
                                    cr2.moveToFirst();
                                    while (!cr2.isAfterLast()) {
                                        str21 = cr2.getString(cr2.getColumnIndex("stamps"));
                                        sb.append("" + str21.toString() + "\n");
                                        cr2.moveToNext();
                                    }
                                    database.close();

                                    int c = pWrite + 1;
                                    databaseP1 = mContext.openOrCreateDatabase("pointer1.db", mContext.MODE_PRIVATE, null);
                                    databaseP1.execSQL("create table if not exists pointerTable1 (point1 INTEGER(10))");
                                    databaseP1.execSQL("insert into pointerTable1(point1) values('" + c + "')");
                                    databaseP1.close();
                                }
                            }
                            String strR = sb.toString();
                            str1 = strR + "\n"; // + pointerSI;
                        }
                    } catch (Exception e) {
                        Log.e("Read Exeption", e.toString());
                    }
                }

                if (unitID != "0000") {
                    try {
                        Log.i("StampsTransmission","sending mail");
                        sender.sendMail(unitID, str1, unitID, toMail);
                        //new SendMail().execute(sender);
                        Log.i("StampsTransmission","mail sent");
                    }
                    catch (Exception ex){
                        Log.i("StampsTransmission","Error:"+ex.getMessage());
                    }

                    Log.v("Main Send report", "Sender Called");

                    try {
                        databaseS = mContext.openOrCreateDatabase("SiSt.db", Context.MODE_PRIVATE, null);
                        databaseS.delete("SiStTable", null, null);
                        databaseS.close();
                    }catch (Exception e){
                    }

                    new MyLogger().storeMassage("StampsTransmission", "End Stamps Transmission");
                    //database.close();
                } else {
                    Log.e("Unit Id", "" + unitID);
                }

            } catch (Exception e) {
                Log.e("StampsTransmission", "InAsyncException:- " + e.getMessage());
                new MyLogger().storeMassage("InAsyncStampsTransmission", "Exception:- " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            globalVariable = (GlobalVariable) getApplicationContext();
            globalVariable.setSendnigFlage(false);
        }
    }
}
