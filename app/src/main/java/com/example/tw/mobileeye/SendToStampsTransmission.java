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

import java.util.Timer;
import java.util.TimerTask;


public class SendToStampsTransmission extends Service {
    private static Timer timer = new Timer();
    private Context ctx;
    SQLiteDatabase database;
    int siDuration1;
    GlobalVariable globalVariable;

    public void onCreate()
    {
        super.onCreate();
        ctx = this;
        startService();
    }

    private void startService()
    {
        try {
            database = openOrCreateDatabase("Config", MODE_PRIVATE, null);
            database.execSQL("create table if not exists file(id INTEGER PRIMARY KEY AUTOINCREMENT,parameter varchar(100) unique,value varchar(150))");
           // siDuration1 = retriveInt("siDuration");
            siDuration1 = 60;
            database.close();
        } catch (Exception e){
        }
        timer.scheduleAtFixedRate(new mainTask(), 60000, siDuration1 * 1000);
    }

    private class mainTask extends TimerTask
    {
        public void run()
        {
            taskHandler.sendEmptyMessage(0);
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
            globalVariable = (GlobalVariable) getApplicationContext();
            if ((globalVariable.isMailSending() == false) && (globalVariable.isSendnigFlage() == true)){
                startService(new Intent(SendToStampsTransmission.this, StampsTransmission.class));
            }
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
}
