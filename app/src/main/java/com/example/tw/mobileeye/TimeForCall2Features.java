package com.example.tw.mobileeye;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;


public class TimeForCall2Features extends Service {
    private static Timer timer = new Timer();
    private Context ctx;
    int interval;
    SQLiteDatabase database;
    String unitID;
    GlobalVariable globalVariable;
    String tag = "TimeForCallToFeature";

    public void onCreate()
    {
        Log.e("TimeForCallToFeature", "onCreate: ");
        super.onCreate();
        ctx = this;
        globalVariable = (GlobalVariable) ctx.getApplicationContext();
        startService();
    }

    private void startService()
    {
        Log.e(tag, "startService: ");
        try {
            database = ctx.openOrCreateDatabase("Config", ctx.MODE_PRIVATE, null);
            database.execSQL("create table if not exists file(id INTEGER PRIMARY KEY AUTOINCREMENT,parameter varchar(100) unique,value varchar(150))");
            interval = retriveInt("siInterval");
            unitID = retrive("unitId");
            Log.e(tag, "unitId: " + unitID );
            database.close();
        } catch (Exception e){
            Log.e(tag, "startService2: " + e.getMessage());
        }

        //timer.scheduleAtFixedRate(new mainTask(), 2000, 1000);
        //startService(new Intent(TimeForCall2Features.this, CallToNmeaListener.class));

        //try
       // {
            int uid = Integer.parseInt(unitID);
        Log.e(tag, "unitId: " + unitID );
            if (uid > 0)
            {
                taskHandler.sendEmptyMessage(0);
                timer.scheduleAtFixedRate(new mainTask2(), 45000, 60*60*1000);
                timer.scheduleAtFixedRate(new mainTask3(), 2000, interval * 1000);
                timer.scheduleAtFixedRate(new mainTask4(), 2000, 10 * 1000);


                ///call to JRM
                callToJRM();

            }
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            Log.e(tag, "startService3: " + e.getMessage());
//        }


    }

    /*private class mainTask extends TimerTask
    {
        public void run()
        {
            taskHandler.sendEmptyMessage(0);
        }
    }*/

    private class mainTask2 extends TimerTask
    {
        public void run()
        {
            taskHandler2.sendEmptyMessage(0);
        }
    }

    private class mainTask3 extends TimerTask
    {
        public void run()
        {
            taskHandler3.sendEmptyMessage(0);
        }
    }

    private class mainTask4 extends TimerTask
    {
        public void run()
        {
            taskHandler4.sendEmptyMessage(0);
        }
    }

    public void onDestroy()
    {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final Handler taskHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            //startService(new Intent(TimeForCall2Features.this, MyService.class));
            startService(new Intent(TimeForCall2Features.this, CallToNmeaListener.class));
        }
    };

    private final Handler taskHandler2 = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            startService(new Intent(TimeForCall2Features.this, UpdateRemoteParameter.class));
        }
    };

    private final Handler taskHandler3 = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            startService(new Intent(TimeForCall2Features.this, GenrateSIandNG.class));
        }
    };

    private final Handler taskHandler4 = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            DistanceCompute dc = new DistanceCompute(ctx);
            dc.getDistance();
        }
    };

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

    private void callToJRM()
    {
        try {
            SQLiteDatabase database = this.openOrCreateDatabase("Command.db", this.MODE_PRIVATE, null);
            database.execSQL("create table if not exists mycommand(cmdno INTEGER PRIMARY KEY AUTOINCREMENT, cmdDateTime DATETIME, cmdText VARCHAR2(80))");
            Cursor cursor = database.rawQuery("select * from mycommand", null);
            String cmd;
            Log.i("***JRM***","In beforetofirst");
            if (cursor.moveToFirst()) {
                Log.i("***JRM***","In movetofirst");
                cmd = cursor.getString(2);
                StringTokenizer st = new StringTokenizer(cmd, ",");
                String jrmStatus, routeID;
                st.nextToken();
                jrmStatus = st.nextToken();Log.i("***JRM***","jrmStatus: "+jrmStatus);
                st.nextToken();
                routeID = st.nextToken();
                if (jrmStatus.equals("JRMON")) {
                    //check and download route
                    globalVariable.setRouteNo(routeID);
                    globalVariable.setJrmON(true);
                    Intent jrmPanelIntent = new Intent(this, JRMTestingActivity.class);
                    jrmPanelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(jrmPanelIntent);
                    Intent jrmIntent = new Intent(this, JRMService.class);
                    startService(jrmIntent);
                } else if (jrmStatus.equals("JRMOF")) {
                    //stop jrm
                    globalVariable.setJrmON(false);
                }
            }
            Log.i("***JRM***","In aftertofirst");
            cursor.close();
            database.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent i = new Intent(TimeForCall2Features.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getBaseContext().startActivity(i);
        super.onTaskRemoved(rootIntent);
    }
}
