package com.example.tw.mobileeye;


import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;

public class UpdateRemoteParameter extends Service {

    SQLiteDatabase database;
    FTPClient ftpClient;
    private int returnCode;
    String dota = "#DOTA";
    String TxIncident = "#ACCD";
    File myFile = new File("/sdcard/IncidentData.txt");
    String unitID, imeiNo, ftp_host, ftp_port, ftp_username, ftp_password, ftp_path, smtpPassword, smtpHost, smtpport, toMailIncident;
    int siDuration1;
    int tCount;
    int sNoI;
    int pReadI, pWriteI;
    StringBuffer sbI, sb11I, sb22I;
    int lastCountI;
    int mailSizeI;
    String codeVersion, modelNumber, simNumber, imeiNumber, protocolType, smtpHost1, smtpPort,
            toMail, ftpHost, ftpUsername, jrmStatus, routeId, rmcString, unitId;
    int txTime, stTime, osVersion, gZoneOS, yZoneOS, rZoneOS;
    String trackFileString, osVersionName;
    GlobalVariable globalVariable;
    Context mContext;
    private static Timer T = new Timer();

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startService1();
        return START_STICKY;
    }

    public void startService1() {


        globalVariable = (GlobalVariable) mContext.getApplicationContext();

        try {
            database = openOrCreateDatabase("Config", MODE_PRIVATE, null);
            database.execSQL("create table if not exists file(id INTEGER PRIMARY KEY AUTOINCREMENT,parameter varchar(100) unique,value varchar(150))");
            ftp_host = retrive("ftpHost");
            ftp_port = retrive("ftpPort");
            ftp_username = retrive("ftpUN");
            ftp_password = retrive("ftpPW");
            ftp_path = retrive("ftpPath");
            unitID = retrive("unitId");
            imeiNo = retrive("imeiNo");
            smtpPassword = retrive("smtpPW");
            smtpHost = retrive("smtpHost");
            smtpport = retrive("smtpPort");
            toMailIncident = retrive("toMailIncident");
            siDuration1 = retriveInt("siDuration");
            lastCountI = retriveInt("INCdbSize");
            mailSizeI = retriveInt("INCmailSize");
            database.close();
        } catch (Exception e) {
        }

        NewAsyncTask nat = new NewAsyncTask();
        nat.execute();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class NewAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            {
                NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
                if (netInfo == null) {
                } else {

                    CheckCommand checkCommand = new CheckCommand(getApplicationContext());
                    checkCommand.get();

                    ftpClient = new FTPClient();
                    try {
                        ftpClient.connect(ftp_host);
                        ftpClient.login(ftp_username, ftp_password);
                        ftpClient.enterLocalPassiveMode();
                        ftpClient.changeWorkingDirectory(ftp_path);

                        if (checkFileExists(ftp_path + "UI" + unitID + ".txt")) {
                            ftpClient.connect(ftp_host);
                            ftpClient.login(ftp_username, ftp_password);
                            ftpClient.enterLocalPassiveMode();
                            ftpClient.changeWorkingDirectory(ftp_path);
                            InputStream inStream = ftpClient.retrieveFileStream("UI" + unitID + ".txt");
                            InputStreamReader isr = new InputStreamReader(inStream, "UTF8");
                            BufferedReader reader = new BufferedReader(isr);
                            String temp = "";
                            StringBuffer buffer = new StringBuffer("");
                            while ((temp = reader.readLine()) != null) {
                                buffer.append(temp);
                            }
                            String result = buffer.toString();

                            Log.e("FTP file string", result);

                            if (result.equals(dota)) {
                                ftpClient.connect(ftp_host);
                                ftpClient.login(ftp_username, ftp_password);
                                ftpClient.enterLocalPassiveMode();
                                ftpClient.changeWorkingDirectory(ftp_path);

                                if (checkFileExists(ftp_path + imeiNo + ".apk")) {
                                    ftpConnect(ftp_host, ftp_username, ftp_password, Integer.parseInt(ftp_port));
                                    /*MyAsyncTask myClass = new MyAsyncTask();
                                    myClass.execute();*/
                                    File file = new File("sdcard/Download/temp.apk");
                                    if (file.exists()) {
                                        file.delete();
                                        try {
                                            ftpDownload(ftp_path + imeiNo + ".apk", "sdcard/Download/temp.apk");
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/Download/temp.apk")), "application/vnd.android.package-archive");
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        } catch (Exception e) {
                                        }
                                    } else {
                                        try {
                                            ftpDownload(ftp_path + imeiNo + ".apk", "sdcard/Download/temp.apk");
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/Download/temp.apk")), "application/vnd.android.package-archive");
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        } catch (Exception e) {
                                        }
                                    }
                                    Log.d("Downloading Started", "Downloading Started");

                                    ftpDisconnect();
                                    Log.d("Downloading Completed", "Downloading Completed");

                                } else {
                                }
                            } else if (result.equals(TxIncident)) {
                                globalVariable.setMailSendingFlage(true);
                                new MyLogger().storeMassage("IncidentTransmission", "Start Transmission");

                                mainTask m = new mainTask();
                                T.scheduleAtFixedRate(m, 1000, 30 * 1000);
                            } else {
                                try {
                                    JSONObject obj = new JSONObject(result);
                                    String smtpHost = obj.getString("smtp_host");
                                    String smtpPort = obj.getString("smtp_port");
                                    String smtpPassword = obj.getString("smtp_password");
                                    String ftpHost = obj.getString("ftp_host");
                                    String ftpPort = obj.getString("ftp_port");
                                    String ftpUsername = obj.getString("ftp_username");
                                    String ftpPassword = obj.getString("ftp_password");
                                    String ftpFilePath = obj.getString("ftp_file_path");
                                    String toMail = obj.getString("to_mail");
                                    String unitIdUrl = obj.getString("unit_id_url");
                                    int siInterval = obj.getInt("si_interval");
                                    int siDuration = obj.getInt("si_duration"); //to_mail_incident
                                    String toMailIncident = obj.getString("to_mail_incident");
                                    int acdcLimit = obj.getInt("acdc_limit");
                                    int osLimit = obj.getInt("os_limit");
                                    int incidentLimit = obj.getInt("incident_limit");
                                    int STM_dbSize = obj.getInt("STM_dbSize");
                                    int INC_dbSize = obj.getInt("INC_dbSize");
                                    int STM_mailSize = obj.getInt("STM_mailSize");
                                    int INC_mailSize = obj.getInt("INC_mailSize");
                                    String commandHost = obj.getString("command_host");
                                    String commandPort = obj.getString("command_port");
                                    String commandUsername = obj.getString("command_username");
                                    String commandPassword = obj.getString("command_password");
                                    String routeHost = obj.getString("route_host");
                                    String routePort = obj.getString("route_port");
                                    String routeUsername = obj.getString("route_username");
                                    String routePassword = obj.getString("route_password");
                                    int rzOsLimit = obj.getInt("rzOs_limit");
                                    int yzOsLimit = obj.getInt("yzOs_limit");
                                    int gzOsLimit = obj.getInt("gzOs_limit");

                                    database = openOrCreateDatabase("Config", MODE_PRIVATE, null);
                                    database.execSQL("create table if not exists file(id INTEGER PRIMARY KEY AUTOINCREMENT,parameter varchar(100) unique,value varchar(150))");
                                    update("smtpHost", smtpHost);
                                    update("smtpPort", smtpPort);
                                    update("smtpPW", smtpPassword);
                                    update("ftpHost", ftpHost);
                                    update("ftpPort", ftpPort);
                                    update("ftpUN", ftpUsername);
                                    update("ftpPW", ftpPassword);
                                    update("ftpPath", ftpFilePath);
                                    update("uIdUrl", unitIdUrl);
                                    update("toMail", toMail);
                                    update("toMailIncident", toMailIncident);
                                    updateInt("siInterval", siInterval);
                                    updateInt("siDuration", siDuration);
                                    updateInt("acdcLimit", acdcLimit);
                                    updateInt("osLimit", osLimit);
                                    updateInt("incidentLimit", incidentLimit);
                                    updateInt("STMdbSize", STM_dbSize);
                                    updateInt("INCdbSize", INC_dbSize);
                                    updateInt("STMmailSize", STM_mailSize);
                                    updateInt("INCmailSize", INC_mailSize);
                                    update("hostCommand", commandHost);
                                    update("portCommand", commandPort);
                                    update("unCommand", commandUsername);
                                    update("pwCommand", commandPassword);
                                    update("hostRoute", routeHost);
                                    update("portRoute", routePort);
                                    update("unRoute", routeUsername);
                                    update("pwRoute", routePassword);
                                    updateInt("rZoneOs", rzOsLimit);
                                    updateInt("yZoneOs", yzOsLimit);
                                    updateInt("gZoneOs", gzOsLimit);
                                    database.close();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("FTPClient Exception is ", "" + e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


            if (globalVariable.isTFSending() == true) {

                try {
                    database = openOrCreateDatabase("Config", MODE_PRIVATE, null);
                    database.execSQL("create table if not exists file(id INTEGER PRIMARY KEY AUTOINCREMENT,parameter varchar(100) unique,value varchar(150))");
                    ftpHost = retrive("ftpHost");
                    ftpUsername = retrive("ftpUN");
                    unitId = retrive("unitId");
                    smtpHost1 = retrive("smtpHost");
                    smtpPort = retrive("smtpPort");
                    toMail = retrive("toMail");
                    txTime = retriveInt("siDuration");
                    stTime = retriveInt("siInterval");
                    rZoneOS = retriveInt("rZoneOs");
                    yZoneOS = retriveInt("yZoneOs");
                    gZoneOS = retriveInt("gZoneOs");
                    database.close();
                } catch (Exception e) {
                }

                Log.e("JRM OS", rZoneOS + ", " + yZoneOS + ", " + gZoneOS);

                String modelName = Build.MODEL;
                String manufacture = Build.MANUFACTURER;
                modelNumber = manufacture + "-" + modelName;

                try {

                    osVersion = Build.VERSION.SDK_INT;
                    switch (osVersion) {
                        case 24:
                            osVersionName = "Android 7.0(Nougat)";
                            break;
                        case 23:
                            osVersionName = "Android 6.0(Marshmallow)";
                            break;
                        case 22:
                            osVersionName = "Android 5.1(LOLLIPOP_MR1)";
                            break;
                        case 21:
                            osVersionName = "Android 5.0(LOLLIPOP)";
                            break;
                        case 20:
                            osVersionName = "Android 4.4W(KITKAT_WATCH)";
                            break;
                        case 19:
                            osVersionName = "Android 4.4(KITKAT)";
                            break;
                        case 18:
                            osVersionName = "Android 4.3(JELLY_BEAN_MR2)";
                            break;
                        case 17:
                            osVersionName = "Android 4.2(JELLY_BEAN_MR1)";
                            break;
                        case 16:
                            osVersionName = "Android 4.1(JELLY_BEAN)";
                            break;
                        case 15:
                            osVersionName = "Android 4.0.3(ICE_CREAM_SANDWICH_MR1)";
                            break;
                        case 14:
                            osVersionName = "Android 4.0(ICE_CREAM_SANDWICH)";
                            break;
                        case 13:
                            osVersionName = "Android 3.2(HONEYCOMB_MR2)";
                            break;
                        case 12:
                            osVersionName = "Android 3.1(HONEYCOMB_MR1)";
                            break;
                        case 11:
                            osVersionName = "Android 3.0(HONEYCOMB)";
                            break;
                        case 10:
                            osVersionName = "Android 2.3(GINGERBREAD_MR1)";
                            break;
                        case 9:
                            osVersionName = "Android 2.3(GINGERBREAD)";
                            break;
                        default:
                            osVersionName = "" + osVersion;
                            break;
                    }
                } catch (Exception e) {
                }


                TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(UpdateRemoteParameter.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                simNumber = tel.getSimSerialNumber().toString();
                imeiNumber = tel.getDeviceId().toString();

                PackageInfo pInfo = null;
                try {
                    pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                codeVersion = pInfo.versionName;

                if (globalVariable.isJrmON() == true){
                    jrmStatus = "JRMON,,,,,avl+jrm,"; //JRMON,,,,,avl+jrm,
                } else {
                    jrmStatus = "JRMOF,,,,,avl,"; //JRMON,,,,,avl+jrm,
                    rZoneOS = 00;
                    yZoneOS = 00;
                    gZoneOS = 00;
                }

                try {
                    rmcString = globalVariable.getRmcString(); //RMC,100826.000,A,1903.0774,N,07254.6044,E,0.00,274.10,110316,,,D
                }catch (Exception e){
                }

                protocolType = "SMTP";
                routeId = globalVariable.getRouteNo();

                trackFileString = "TF,CV:"+codeVersion+",WN:"+modelNumber+",SM:"+simNumber+",FW:"+osVersionName+
                        ",IMEI:"+imeiNumber+",TT:"+protocolType+",SS:"+smtpHost1+",Fr:"+smtpPort+",To:"+toMail+",DFS:"+ftpHost+",DUN:"+ftpUsername+
                        ",VC1:,VC2:,Tx:"+txTime+",St:"+stTime+",APN:www,OS,OS1,OS2:"+gZoneOS+","+yZoneOS+","+rZoneOS+",JS:"+jrmStatus+
                        ",RouteID:"+routeId+",RMC STRING:"+rmcString;

                new sendTFasyncTask().execute();
            }

            startService(new Intent(UpdateRemoteParameter.this, SendToStampsTransmission.class));

        }
    }

    private class mainTask extends TimerTask
    {
        public void run()
        {
            taskHandler.sendEmptyMessage(0);
        }
    }

    private final Handler taskHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            //startService(new Intent(TimeForCall2Features.this, MyService.class));
            File databaseExist = getDatabasePath("incidentData.db");
            if (databaseExist.exists()) {
                TextView tv = new TextView(getApplicationContext());
                tv.setId(0);
                database = openOrCreateDatabase("incidentData.db", MODE_PRIVATE, null);
                Cursor cr = database.rawQuery("Select * from incidentDataTable", null);
                cr.moveToFirst();
                while (!cr.isAfterLast()) {
                    sNoI = cr.getInt(cr.getColumnIndex("srNo1"));
                    cr.moveToNext();
                }
                database.close();
                //========================================>

                File databaseExist0 = getDatabasePath("pointerI.db");
                if (databaseExist0.exists()) {
                    database = openOrCreateDatabase("pointerI.db", MODE_PRIVATE, null);
                    Cursor cr0 = database.rawQuery("Select *from pointerTableI", null);
                    cr0.moveToFirst();
                    while (!cr0.isAfterLast()) {
                        pWriteI = cr0.getInt(cr0.getColumnIndex("pointI"));
                        cr0.moveToNext();
                    }
                    database.close();
                }

                if (sNoI == lastCountI) {
                    pWriteI = pWriteI - 1;
                } else {
                    pWriteI = sNoI;
                }

                pReadI = 1;
                File databaseExist1 = getDatabasePath("pointer2.db");
                if (databaseExist1.exists()) {
                    database = openOrCreateDatabase("pointer2.db", MODE_PRIVATE, null);
                    Cursor cr0 = database.rawQuery("Select *from pointerTable2", null);
                    cr0.moveToFirst();
                    while (!cr0.isAfterLast()) {
                        pReadI = cr0.getInt(cr0.getColumnIndex("point2"));
                        cr0.moveToNext();
                    }
                    database.close();
                }

                int ll = pReadI - 1;
                if (ll == pWriteI) {
                    T.cancel();
                    globalVariable.setMailSendingFlage(false);
                    new MyLogger().storeMassage("IncidentTransmission", "Timer cancel");
                } else {
                    if (pReadI > pWriteI) {
                        int d = lastCountI - pReadI;
                        if (d < mailSizeI) {
                            database = openOrCreateDatabase("incidentData.db", MODE_PRIVATE, null);
                            Cursor cr2 = database.rawQuery("select * from incidentDataTable where srNo1 >= '" + pReadI + "' and srNo1 <= '" + lastCountI + "'", null);
                            sb11I = new StringBuffer();
                            cr2.moveToFirst();
                            while (!cr2.isAfterLast()) {
                                String str5 = cr2.getString(cr2.getColumnIndex("incident"));
                                sb11I.append("" + str5.toString() + "\n");
                                cr2.moveToNext();
                            }
                            Cursor cr3 = database.rawQuery("select * from incidentDataTable where srNo1 = '" + pWriteI + "'", null);
                            sb22I = new StringBuffer();
                            cr3.moveToFirst();
                            while (!cr3.isAfterLast()) {
                                String str5 = cr3.getString(cr3.getColumnIndex("incident"));
                                sb22I.append("" + str5.toString() + "\n");
                                cr3.moveToNext();
                            }
                            sbI = new StringBuffer();
                            sbI.append("" + sb11I + sb22I + "\n");
                            database.close();

                            int c = 1;
                            database = openOrCreateDatabase("pointer2.db", MODE_PRIVATE, null);
                            database.execSQL("create table if not exists pointerTable2 (point2 INTEGER(10))");
                            database.execSQL("insert into pointerTable2(point2) values('" + c + "')");
                            database.close();
                        } else {
                            int l = pReadI + mailSizeI;
                            database = openOrCreateDatabase("incidentData.db", MODE_PRIVATE, null);
                            Cursor cr2 = database.rawQuery("select * from incidentDataTable where srNo1 >= '" + pReadI + "' and srNo1 <= '" + l + "'", null);
                            sb11I = new StringBuffer();
                            cr2.moveToFirst();
                            while (!cr2.isAfterLast()) {
                                String str5 = cr2.getString(cr2.getColumnIndex("incident"));
                                sb11I.append("" + str5.toString() + "\n");
                                cr2.moveToNext();
                            }
                            Cursor cr3 = database.rawQuery("select * from incidentDataTable where srNo1 = '" + pWriteI + "'", null);
                            sb22I = new StringBuffer();
                            cr3.moveToFirst();
                            while (!cr3.isAfterLast()) {
                                String str5 = cr3.getString(cr3.getColumnIndex("incident"));
                                sb22I.append("" + str5.toString() + "\n");
                                cr3.moveToNext();
                            }
                            sbI = new StringBuffer();
                            sbI.append("" + sb11I + sb22I + "\n");
                            database.close();

                            int c = l + 1;
                            database = openOrCreateDatabase("pointer2.db", MODE_PRIVATE, null);
                            database.execSQL("create table if not exists pointerTable2 (point2 INTEGER(10))");
                            database.execSQL("insert into pointerTable2(point2) values('" + c + "')");
                            database.close();
                        }
                    } else {
                        int size = pWriteI - pReadI;
                        if (size > mailSizeI) {
                            int d = lastCountI - pReadI;
                            if (d < mailSizeI) {
                                database = openOrCreateDatabase("incidentData.db", MODE_PRIVATE, null);
                                Cursor cr2 = database.rawQuery("select * from incidentDataTable where srNo1 >= '" + pReadI + "' and srNo1 <= '" + lastCountI + "'", null);
                                sb11I = new StringBuffer();
                                cr2.moveToFirst();
                                while (!cr2.isAfterLast()) {
                                    String str5 = cr2.getString(cr2.getColumnIndex("incident"));
                                    sb11I.append("" + str5.toString() + "\n");
                                    cr2.moveToNext();
                                }
                                Cursor cr3 = database.rawQuery("select * from incidentDataTable where srNo1 = '" + pWriteI + "'", null);
                                sb22I = new StringBuffer();
                                cr3.moveToFirst();
                                while (!cr3.isAfterLast()) {
                                    String str5 = cr3.getString(cr3.getColumnIndex("incident"));
                                    sb22I.append("" + str5.toString() + "\n");
                                    cr3.moveToNext();
                                }
                                sbI = new StringBuffer();
                                sbI.append("" + sb11I + sb22I + "\n");
                                database.close();

                                int c = 1;
                                database = openOrCreateDatabase("pointer2.db", MODE_PRIVATE, null);
                                database.execSQL("create table if not exists pointerTable2 (point2 INTEGER(10))");
                                database.execSQL("insert into pointerTable2(point2) values('" + c + "')");
                                database.close();
                            } else {
                                int l = pReadI + mailSizeI;
                                database = openOrCreateDatabase("incidentData.db", MODE_PRIVATE, null);
                                Cursor cr2 = database.rawQuery("select * from incidentDataTable where srNo1 >= '" + pReadI + "' and srNo1 <= '" + l + "'", null);
                                sb11I = new StringBuffer();
                                cr2.moveToFirst();
                                while (!cr2.isAfterLast()) {
                                    String str5 = cr2.getString(cr2.getColumnIndex("incident"));
                                    sb11I.append("" + str5.toString() + "\n");
                                    cr2.moveToNext();
                                }
                                Cursor cr3 = database.rawQuery("select * from incidentDataTable where srNo1 = '" + pWriteI + "'", null);
                                sb22I = new StringBuffer();
                                cr3.moveToFirst();
                                while (!cr3.isAfterLast()) {
                                    String str5 = cr3.getString(cr3.getColumnIndex("incident"));
                                    sb22I.append("" + str5.toString() + "\n");
                                    cr3.moveToNext();
                                }
                                sbI = new StringBuffer();
                                sbI.append("" + sb11I + sb22I + "\n");
                                database.close();

                                int c = l + 1;
                                database = openOrCreateDatabase("pointer2.db", MODE_PRIVATE, null);
                                database.execSQL("create table if not exists pointerTable2 (point2 INTEGER(10))");
                                database.execSQL("insert into pointerTable2(point2) values('" + c + "')");
                                database.close();
                            }
                        } else {
                            database = openOrCreateDatabase("incidentData.db", MODE_PRIVATE, null);
                            Cursor cr2 = database.rawQuery("select * from incidentDataTable where srNo1 >= '" + pReadI + "' and srNo1 <= '" + pWriteI + "'", null);
                            sbI = new StringBuffer();
                            cr2.moveToFirst();
                            while (!cr2.isAfterLast()) {
                                String str6 = cr2.getString(cr2.getColumnIndex("incident"));
                                sbI.append("" + str6.toString() + "\n");
                                cr2.moveToNext();
                            }
                            database.close();

                            int c = pWriteI + 1;
                            database = openOrCreateDatabase("pointer2.db", MODE_PRIVATE, null);
                            database.execSQL("create table if not exists pointerTable2 (point2 INTEGER(10))");
                            database.execSQL("insert into pointerTable2(point2) values('" + c + "')");
                            database.close();
                        }
                    }

                    String strI = sbI.toString();
                    try {
                        myFile.createNewFile();
                        FileOutputStream fos = new FileOutputStream(myFile);
                        OutputStreamWriter myOutWriter11 = new OutputStreamWriter(fos);
                        myOutWriter11.write(strI);
                        myOutWriter11.close();
                        fos.close();
                                                            /*MyAsyncTask1 myClass1 = new MyAsyncTask1();
                                                            myClass1.execute();*/

                        Log.v("Sendreport " + unitID, "" + smtpHost + " " + smtpPassword + " " + smtpport);
                        TWattachmentMailSender sender = new TWattachmentMailSender(unitID, smtpPassword, smtpHost, smtpport);
                        try {
                            sender.sendMail1(unitID, "", "/sdcard/IncidentData.txt", unitID, toMailIncident);
                            Log.v("Incident Sendreport", "Sendreport Called");
                            myFile.delete();
                            new MyLogger().storeMassage("IncidentTransmission", "Stop Transmission");
                        } catch (Exception e) {
                            Log.v("Exception Sendreport", "" + e);
                            new MyLogger().storeMassage("Incident sending Exception :- ", e.toString());
                            e.printStackTrace();
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.v("File Create", "" + e);
                    }
                }
            }
        }
    };

    class sendTFasyncTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {

            TWsimpleMailSender sender = new TWsimpleMailSender(unitID, smtpPassword, smtpHost1, smtpPort);
            try {
                sender.sendMail(unitID, trackFileString, unitID, toMail);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            globalVariable.setTFSendingFlage(false);
        }
    }

    class MyAsyncTask1 extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Log.v("Sendreport " + unitID, "" + smtpHost + " " + smtpPassword + " " + smtpport);
            TWattachmentMailSender sender = new TWattachmentMailSender(unitID, smtpPassword, smtpHost, smtpport);
            try {
                sender.sendMail1(unitID, "", "/sdcard/IncidentData.txt", unitID, toMailIncident);
                Log.v("Incident Sendreport", "Sendreport Called");
            } catch (Exception e) {
                Log.v("Exception Sendreport", "" + e);
                new MyLogger().storeMassage("Incident sending Exception :- ", e.toString());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            myFile.delete();
            new MyLogger().storeMassage("IncidentTransmission", "Stop Transmission");
        }
    }

    class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            File file =new File("sdcard/Download/temp.apk");
            if(file.exists()) {
                file.delete();
                try {
                    ftpDownload(ftp_path + imeiNo + ".apk", "sdcard/Download/temp.apk");
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/Download/temp.apk")), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                }
            } else {
                try {
                    ftpDownload(ftp_path + imeiNo + ".apk", "sdcard/Download/temp.apk");
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/Download/temp.apk")), "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                }
            }
            Log.d("Downloading Started","Downloading Started");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ftpDisconnect();
            Log.d("Downloading Completed","Downloading Completed");
        }
    }

     boolean checkFileExists(String filePath) throws IOException {
        InputStream inputStream = ftpClient.retrieveFileStream(filePath);
        returnCode = ftpClient.getReplyCode();
        if (inputStream == null || returnCode == 550) {
            return false;
        }
        return true;
    }

    private String update(String p, String v) {
        try {
            database.execSQL("update file set value='" + v + "' where parameter ='" + p + "'");
            return "Success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private String updateInt(String p, int v) {
        try {
            database.execSQL("update file set value='" + v + "' where parameter ='" + p + "'");
            return "Success";
        } catch (Exception e) {
            return e.getMessage();
        }
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

    public boolean ftpConnect(String host, String username, String password, int port) {
        try {
            ftpClient = new FTPClient();
            ftpClient.connect(host, port);
            if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                boolean status = ftpClient.login(username, password);
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                return status;
            }
        } catch (Exception e) {
            Log.d("ftpConnect", "Error: could not connect to host " + host);
        }
        return false;
    }

    public boolean ftpDownload(String srcFilePath, String desFilePath) {
        boolean status = false;
        try {
            FileOutputStream desFileStream = new FileOutputStream(desFilePath);
            status = ftpClient.retrieveFile(srcFilePath, desFileStream);
            desFileStream.close();
            return status;
        } catch (Exception e) {
            Log.d("ftpDownload", "download failed");
        }
        return status;
    }

    public boolean ftpDisconnect() {
        try {
            ftpClient.logout();
            ftpClient.disconnect();
            Log.d("ftpDisconnect", "Disconected from FTP on apk Download");
            return true;
        } catch (Exception e) {
            Log.d("ftpDisconnect", "Error occurred while disconnecting from ftp server on apk download.");
        }
        return false;
    }
}
