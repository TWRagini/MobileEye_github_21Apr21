package com.example.tw.mobileeye;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;

public class JRMService extends Service {

    Context myContext;
    int i = 0, j = 0, curPoint = 0, count = 0, comp = 0, jrmFun = 0;
    String zone, routeNo;
    float mdist1, mdist2;
    double dbdist1, dbdist2, dbdist3, addition, difference, prevDist;
    //unsigned double difference;
    boolean onroute = false, forwardJourney = true, returnJourney = false, zone_detection = false, zone_dv = false, pre_zone = false;
    ArrayList<RoutePoint> routePointsList;
    Location myLocation = new Location("");
    Location locationA = new Location("");
    Location locationB = new Location("");
    Timer timerDetectRoute, timerFindNextZone, timerCheckZone;
    TimerTask timerTaskDetectRoute, timerTaskFindNextZone, timerTaskCheckZone;
    private final IBinder mBinder = new LocalBinder();
    //JRMPanelActivity mainActivity;
    public static final String zoneAction = "UpdateZoneReceiver.ChangeZone";
    public static final String jrmActivityAction = "StopJRMPanel";
    public static final String jrmChangeAction = "ChangeJRM";
    Intent intentZoneUpdate;
    MediaPlayer mediaPlayer;
    double latitude1, longitude1;
    GlobalVariable globalVariable;


    public JRMService() {
        this.myContext = this;
        //Toast.makeText(JRMService.this, "JRM Constructor", Toast.LENGTH_SHORT).show();
        Log.i("***JRM***", "***Constructor***");
    }

    public class LocalBinder extends Binder {
        public JRMService getServiceInstance() {
            return JRMService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        routeNo = intent.getStringExtra("RouteNo");
        Toast.makeText(JRMService.this, "JRM Started", Toast.LENGTH_SHORT).show();
       // gpsService = new GpsService(this);
        checkAndDownloadRoute(routeNo);
        return mBinder;
    }

    @Override
    public void onCreate() {
        //super.onCreate();
        //intentZoneUpdate = new Intent(zoneAction);

        IntentFilter intentChangeJRM = new IntentFilter(jrmChangeAction);
        ChangeJRMRoute changeJRMRoute = new ChangeJRMRoute();
        LocalBroadcastManager.getInstance(myContext).registerReceiver(changeJRMRoute, intentChangeJRM);

        globalVariable = (GlobalVariable) myContext.getApplicationContext();
        if (timerDetectRoute != null) {
            timerDetectRoute.cancel();
            timerDetectRoute.purge();
        }
        routeNo = "Route" + globalVariable.getRouteNo();

        zone_detection = false;
        zone_dv = false;
        pre_zone = false;
        globalVariable.setPointID("");
        globalVariable.setOsRed(false);
        globalVariable.setOsYellow(false);

        Toast.makeText(JRMService.this, "JRM Started", Toast.LENGTH_SHORT).show();
        checkAndDownloadRoute(routeNo);

        mediaPlayer = MediaPlayer.create(this, R.raw.in_red_zone_speed_limit_30);

        /*For logs*//*
        String filePath = Environment.getExternalStorageDirectory() + "/logcat.txt";
        try {
            Runtime.getRuntime().exec(new String[]{"logcat", "-f", filePath, "MyAppTag:V ***JRM***:I", "*:S"});
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /*globalVariable = (GlobalVariable) myContext.getApplicationContext();

        if (timerDetectRoute != null) {
            timerDetectRoute.cancel();
            timerDetectRoute.purge();
        }
        routeNo = intent.getStringExtra("RouteNo");
        Toast.makeText(JRMService.this, "JRM Started", Toast.LENGTH_SHORT).show();
        checkAndDownloadRoute(routeNo);

        mediaPlayer = MediaPlayer.create(this, R.raw.in_red_zone_speed_limit_30);*/

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //super.onDestroy();
        Toast.makeText(JRMService.this, "JRM Stopped", Toast.LENGTH_SHORT).show();
    }

    public void stopJRM() {
        if (timerDetectRoute != null) {
            timerDetectRoute.cancel();
            timerDetectRoute.purge();
        }
        /*if (timerFindNextZone != null) {
            timerFindNextZone.cancel();
            timerFindNextZone.purge();
        }
        if (timerCheckZone != null) {
            timerCheckZone.cancel();
            timerCheckZone.purge();
        }*/
        //Toast.makeText(JRMService.this, "Stop JRM", Toast.LENGTH_SHORT).show();
        globalVariable.setRouteNo("");
        globalVariable.setPointID("");
        globalVariable.setZone("");
        globalVariable.setOsRed(false);
        globalVariable.setOsYellow(false);
        globalVariable.setJrmON(false);

        Intent intentJRMActivity = new Intent(jrmActivityAction);
        //intentJRMActivity.putExtra("", "stop");
        LocalBroadcastManager.getInstance(myContext).sendBroadcast(intentJRMActivity);


        stopSelf();
        Log.i("***JRM***", "JRM Stop");
        //mainActivity.txtRoute.setText("Hello");
    }

    private class ChangeJRMRoute extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {
            //stopJRM();
            jrmChangeRoute();
        }
    }

    private void jrmChangeRoute()
    {
        if (timerDetectRoute != null) {
            timerDetectRoute.cancel();
            timerDetectRoute.purge();
        }
        globalVariable.setPointID("");
        globalVariable.setZone("");
        globalVariable.setOsRed(false);
        globalVariable.setOsYellow(false);

        routeNo = "Route" + globalVariable.getRouteNo();

        zone_detection = false;
        zone_dv = false;
        pre_zone = false;
        j=0;
        checkAndDownloadRoute(routeNo);

        intentZoneUpdate = new Intent(zoneAction);
        intentZoneUpdate.putExtra("zone", "A");
        intentZoneUpdate.putExtra("pid", "");
        LocalBroadcastManager.getInstance(myContext).sendBroadcast(intentZoneUpdate);

    }


    private Handler handlerDetectRoute = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            //super.handleMessage(msg);

            if (globalVariable.isJrmON()) {
                switch (jrmFun) {
                    case 0:
                        j = 0;
                        detectRoute();
                        break;
                    case 1:
                        findNextZone();
                        break;
                    case 2:
                        j = 0;
                        checkZone();
                        break;
                }
            }
            else
            {
                stopJRM();
            }
        }
    };

    private void detectRoute()
    {
        globalVariable.setPointID("");
        globalVariable.setZone("A");
        globalVariable.setOsRed(false);
        globalVariable.setOsYellow(false);
        intentZoneUpdate = new Intent(zoneAction);
        intentZoneUpdate.putExtra("zone", "A");
        intentZoneUpdate.putExtra("pid", "");
        LocalBroadcastManager.getInstance(myContext).sendBroadcast(intentZoneUpdate);
        if (returnJourney == true) {

            Log.i("***JRM***", "In detect route return");
            if(!(i>=0))
            {
                i = routePointsList.size()-3;
                Log.i("***JRM***", "Not found");
            }
            if (i >= routePointsList.size()-3 ) {
                forwardJourney = false;
                returnJourney = true;
            }


            myLocation.setLatitude(globalVariable.getLatitude());
            myLocation.setLongitude(globalVariable.getLongitude());

            locationA.setLatitude(routePointsList.get(i+1).getLat());
            locationA.setLongitude(routePointsList.get(i+1).getLng());
            locationB.setLatitude(routePointsList.get(i).getLat());
            locationB.setLongitude(routePointsList.get(i).getLng());

            mdist1 = calculateDistance(locationA, myLocation);
            mdist2 = calculateDistance(myLocation, locationB);
            dbdist1 = routePointsList.get(i+1).getDist();
            dbdist2 = routePointsList.get(i).getDist();
            dbdist3 = dbdist1 - mdist1;
            difference = dbdist1 - dbdist2;
                /*if (difference < 0 && dbdist1 == 0) {
                    difference = difference * (-1);
                    dbdist1 = dbdist3 + 1;
                }*/
            Log.i("***JRM***", "i:" + i);
            Log.i("***JRM***", "MyLoc Lat:" + myLocation.getLatitude() + " | Long:" + myLocation.getLongitude());
            /*Log.i("***JRM***", "LocA Lat:" + locationA.getLatitude() + " | Long:" + locationA.getLongitude());
            Log.i("***JRM***", "LocB Lat:" + locationB.getLatitude() + " | Long:" + locationB.getLongitude());*/
            Log.i("***JRM***", "mdist1:" + mdist1 + " | mdist2:" + mdist2 + " | dbdist1:" + dbdist1 + " | dbdist2:" + dbdist2 + " | dbdist3:" + dbdist3 + " | diff:" + difference);
            if (mdist1 == 0) {
                if(i < routePointsList.size()-3) {
                    i++;
                }
                zone = routePointsList.get(i).getZone();
                count++;
                Log.i("***JRMRouteDetected***", "zone: " + zone + " count: " + count);
                setCurrentZone(zone);
                //curPoint = i;
                onroute = true;
                /*timerDetectRoute.cancel();
                timerDetectRoute.purge();*/
                if (i == 0) {
                    forwardJourney = true;
                    returnJourney = false;
                }
                //jrmTimerFindNext();
                //findDirection();
                /*jrmTimerCheck();*/
                jrmFun = 2;

            } else if (mdist2 == 0) {
                //i--;
                zone = routePointsList.get(i).getZone();
                count++;
                Log.i("***JRMRouteDetected***", "zone: " + zone + " count: " + count);
                setCurrentZone(zone);
                //curPoint = i;
                onroute = true;
                /*timerDetectRoute.cancel();
                timerDetectRoute.purge();
                //jrmTimerFindNext();
                //findDirection();
                jrmTimerCheck();*/
                jrmFun = 2;
            } else if ((dbdist3 > dbdist2) && (dbdist3 < dbdist1) && (mdist2 <= difference))   //route detected
            {
                zone = routePointsList.get(i).getZone();
                count++;
                Log.i("***JRMRouteDetected***", "zone: " + zone + " count: " + count);
                setCurrentZone(zone);
                //curPoint = i;
                onroute = true;
                /*timerDetectRoute.cancel();
                timerDetectRoute.purge();
                //jrmTimerFindNext();
                //findDirection();
                jrmTimerCheck();*/
                jrmFun = 2;
            } else {
                //findDirection();
                if (((dbdist3 - dbdist2) <= 0.5) && ((dbdist3 - dbdist2) > 0.0))
                {
                    preZoneDetection(i);
                }
                //jrmCheckDeviation();
                onroute = false;
                i--;
            }
        }
        else { //forwardJourney

            Log.i("***JRM***", "In detect route forward");
            if (!(i <= routePointsList.size() - 3)) {
                /*timerDetectRoute.cancel();
                timerDetectRoute.purge();*/
                Log.i("***JRM***", "Not found");
                i = 0;
            }
            if (i == 0) {
                forwardJourney = true;
                returnJourney = false;
            }

            myLocation.setLatitude(globalVariable.getLatitude());
            myLocation.setLongitude(globalVariable.getLongitude());

            locationA.setLatitude(routePointsList.get(i).getLat());
            locationA.setLongitude(routePointsList.get(i).getLng());
            locationB.setLatitude(routePointsList.get(i + 1).getLat());
            locationB.setLongitude(routePointsList.get(i + 1).getLng());

            mdist1 = calculateDistance(locationA, myLocation);
            mdist2 = calculateDistance(myLocation, locationB);
            dbdist1 = routePointsList.get(i).getDist();
            dbdist2 = routePointsList.get(i + 1).getDist();
            dbdist3 = mdist1 + dbdist1;
            difference = dbdist2 - dbdist1;
                /*if (difference < 0 && dbdist2 == 0) {
                    difference = difference * (-1);
                    dbdist2 = dbdist3 + 1;
                }*/
            Log.i("***JRM***", "i:" + i);
            Log.i("***JRM***", "MyLoc Lat:" + myLocation.getLatitude() + " | Long:" + myLocation.getLongitude());
            /*Log.i("***JRM***", "LocA Lat:" + locationA.getLatitude() + " | Long:" + locationA.getLongitude());
            Log.i("***JRM***", "LocB Lat:" + locationB.getLatitude() + " | Long:" + locationB.getLongitude());*/
            Log.i("***JRM***", "mdist1:" + mdist1 + " | mdist2:" + mdist2 + " | dbdist1:" + dbdist1 + " | dbdist2:" + dbdist2 + " | dbdist3:" + dbdist3 + " | diff:" + difference);
            if (mdist1 == 0) {
                zone = routePointsList.get(i).getZone();
                count++;
                Log.i("***JRMRouteDetected***", "zone: " + zone + " count: " + count);
                setCurrentZone(zone);
                //curPoint = i;
                onroute = true;
                /*timerDetectRoute.cancel();
                timerDetectRoute.purge();
                //jrmTimerFindNext();
                //findDirection();
                jrmTimerCheck();*/
                jrmFun = 2;
            } else if (mdist2 == 0) {
                if (i < routePointsList.size()-3)
                {
                    i++;
                }
                zone = routePointsList.get(i).getZone();
                count++;
                Log.i("***JRMRouteDetected***", "zone: " + zone + " count: " + count);
                setCurrentZone(zone);
                //curPoint = i;
                onroute = true;
                /*timerDetectRoute.cancel();
                timerDetectRoute.purge();
                //jrmTimerFindNext();
                //findDirection();
                jrmTimerCheck();*/
                jrmFun = 2;
            } else if ((dbdist3 > dbdist1) && (dbdist3 < dbdist2) && (mdist2 <= difference))   //route detected
            {
                zone = routePointsList.get(i).getZone();
                count++;
                Log.i("***JRMRouteDetected***", "zone: " + zone + " count: " + count);
                setCurrentZone(zone);
                //curPoint = i;
                onroute = true;
                /*timerDetectRoute.cancel();
                timerDetectRoute.purge();
                //jrmTimerFindNext();
                //findDirection();
                jrmTimerCheck();*/
                jrmFun = 2;
            } else {
                //findDirection();
                if (((dbdist2 - dbdist3) <= 0.5) && ((dbdist2 - dbdist3) > 0.0))
                {
                    preZoneDetection(i);
                }
                //jrmCheckDeviation();
                onroute = false;
                i++;
            }
        }
    }

    private void findNextZone()
    {
        if (returnJourney == true) {
            Log.i("***JRM***", "Inside find next zone returnJourney");

            /*myLocation.setLatitude(globalVariable.getLatitude());
            myLocation.setLongitude(globalVariable.getLongitude());
            //locationA = new Location("");
            locationA.setLatitude(routePointsList.get(i+1).getLat());
            locationA.setLongitude(routePointsList.get(i+1).getLng());
            //locationB = new Location("");
            locationB.setLatitude(routePointsList.get(i).getLat());
            locationB.setLongitude(routePointsList.get(i).getLng());

            mdist1 = calculateDistance(locationA, myLocation);
            mdist2 = calculateDistance(myLocation, locationB);
            dbdist1 = routePointsList.get(i+1).getDist();
            dbdist2 = routePointsList.get(i).getDist();
            dbdist3 = dbdist1-mdist1;
            difference = dbdist1-dbdist2;*/

            if ((i >= 0) && onroute == true) {

                if (j < 20) {
                    myLocation.setLatitude(globalVariable.getLatitude());
                    myLocation.setLongitude(globalVariable.getLongitude());

                    //locationA = new Location("");
                    locationA.setLatitude(routePointsList.get(i+1).getLat());
                    locationA.setLongitude(routePointsList.get(i+1).getLng());
                    //locationB = new Location("");
                    locationB.setLatitude(routePointsList.get(i).getLat());
                    locationB.setLongitude(routePointsList.get(i).getLng());

                    mdist1 = calculateDistance(locationA, myLocation);
                    mdist2 = calculateDistance(myLocation, locationB);
                    dbdist1 = routePointsList.get(i+1).getDist();
                    dbdist2 = routePointsList.get(i).getDist();
                    dbdist3 = dbdist1-mdist1;
                    difference = dbdist1-dbdist2;
                        /*if (difference < 0 && dbdist1 == 0) {
                            difference = difference * (-1);
                            dbdist1 = dbdist3 + 1;
                        }*/
                    Log.i("***JRM***", "i:" + i);
                    Log.i("***JRM***", "MyLoc Lat:" + myLocation.getLatitude() + " | Long:" + myLocation.getLongitude());
                    /*Log.i("***JRM***", "LocA Lat:" + locationA.getLatitude() + " | Long:" + locationA.getLongitude());
                    Log.i("***JRM***", "LocB Lat:" + locationB.getLatitude() + " | Long:" + locationB.getLongitude());*/
                    Log.i("***JRM***", "mdist1:" + mdist1 + " | mdist2:" + mdist2 + " | dbdist1:" + dbdist1 + " | dbdist2:" + dbdist2 + " | dbdist3:" + dbdist3 + " | diff:" + difference);

                    if (((dbdist3 - dbdist2) <= 0.5) && ((dbdist3 - dbdist2) > 0.0))
                    {
                        preZoneDetection(i);
                    }

                    if (mdist1 == 0) {
                        if(i < routePointsList.size()-3) {
                            i++;
                        }
                        zone = routePointsList.get(i).getZone();
                        count++;
                        Log.i("***JRM***", "i:" + i + " zone: " + zone + " count: " + count);
                        setCurrentZone(zone);
                        //curPoint = i;
                        //j = 0;
                        onroute = true;
                        /*timerFindNextZone.cancel();
                        timerFindNextZone.purge();
                            *//*if (i == 0) {
                                forwardJourney = true;
                                returnJourney = false;
                            } else {
                                //findDirection();
                            }*//*
                        jrmTimerCheck();*/
                        jrmFun = 2;
                    } else if (mdist2 == 0) {
                        //i--;
                        zone = routePointsList.get(i).getZone();
                        count++;
                        Log.i("***JRM***", "i:" + i + " zone: " + zone + " count: " + count);
                        setCurrentZone(zone);
                        //curPoint = i;
                        //j = 0;
                        onroute = true;
                        /*timerFindNextZone.cancel();
                        timerFindNextZone.purge();
                            *//*if (i == 0) {
                                forwardJourney = true;
                                returnJourney = false;
                            } else {
                                //findDirection();
                            }*//*
                        jrmTimerCheck();*/
                        jrmFun = 2;
                    } else if ((dbdist3 > dbdist2) && (dbdist3 < dbdist1) && (mdist2 <= difference)) {
                        zone = routePointsList.get(i).getZone();
                        count++;
                        Log.i("***JRM***", "i:" + i + " zone: " + zone + " count: " + count);
                        setCurrentZone(zone);
                        //curPoint = i;
                        //j = 0;
                        onroute = true;
                        //checkZoneContinuously(locationA, locationB, routePointsList, curPoint);
                        //break;
                        /*timerFindNextZone.cancel();
                        timerFindNextZone.purge();
                            *//*if (i == 0) {
                                forwardJourney = true;
                                returnJourney = false;
                            }*//*
                        jrmTimerCheck();*/
                        jrmFun = 2;
                    } else {
                        if ((curPoint - i) == 0)
                        {
                            jrmCheckDeviation();
                        }
                        if (((curPoint - i) < 10) && (i > 0)) {
                            i--;
                            Log.i("***JRM***", "else i--");
                        } else {
                            Log.i("***JRM***", "else inside else j++");
                            i = curPoint;

                            j++;
                        }
                        //findDirection();
                        //onroute = false;
                        //continue;
                    }
                } else if (j == 20) {
                    //jrmCheckDeviation();
                    onroute = false;
                }

            } else {
                //i = curPoint;
                /*timerFindNextZone.cancel();
                timerFindNextZone.purge();*/
                ///Log.i("***JRMRoute***", "Deviation");
                //jrmCheckDeviation();
                i = routePointsList.size()-3;
                /*jrmTimer();*/
                jrmFun = 0;
            }

        }
        else { //forwardJourney==true
            Log.i("***JRM***", "Inside find next zone forwardJourney");

            /*myLocation.setLatitude(globalVariable.getLatitude());
            myLocation.setLongitude(globalVariable.getLongitude());
            //locationA = new Location("");
            locationA.setLatitude(routePointsList.get(i).getLat());
            locationA.setLongitude(routePointsList.get(i).getLng());
            //locationB = new Location("");
            locationB.setLatitude(routePointsList.get(i + 1).getLat());
            locationB.setLongitude(routePointsList.get(i + 1).getLng());

            mdist1 = calculateDistance(locationA, myLocation);
            mdist2 = calculateDistance(myLocation, locationB);
            dbdist1 = routePointsList.get(i).getDist();
            dbdist2 = routePointsList.get(i + 1).getDist();
            dbdist3 = mdist1 + dbdist1;
            difference = dbdist2 - dbdist1;*/

            if ((i < routePointsList.size() - 2) && onroute == true) {

                if (j < 20) {
                    myLocation.setLatitude(globalVariable.getLatitude());
                    myLocation.setLongitude(globalVariable.getLongitude());
                    //locationA = new Location("");
                    locationA.setLatitude(routePointsList.get(i).getLat());
                    locationA.setLongitude(routePointsList.get(i).getLng());
                    //locationB = new Location("");
                    locationB.setLatitude(routePointsList.get(i + 1).getLat());
                    locationB.setLongitude(routePointsList.get(i + 1).getLng());

                    mdist1 = calculateDistance(locationA, myLocation);
                    mdist2 = calculateDistance(myLocation, locationB);
                    dbdist1 = routePointsList.get(i).getDist();
                    dbdist2 = routePointsList.get(i + 1).getDist();
                    dbdist3 = mdist1 + dbdist1;
                    difference = dbdist2 - dbdist1;
                        /*if (difference < 0 && dbdist2 == 0) {
                            difference = difference * (-1);
                            dbdist2 = dbdist3 + 1;
                        }*/
                    Log.i("***JRM***", "i:" + i);
                    Log.i("***JRM***", "MyLoc Lat:" + myLocation.getLatitude() + " | Long:" + myLocation.getLongitude());
                    /*Log.i("***JRM***", "LocA Lat:" + locationA.getLatitude() + " | Long:" + locationA.getLongitude());
                    Log.i("***JRM***", "LocB Lat:" + locationB.getLatitude() + " | Long:" + locationB.getLongitude());*/
                    Log.i("***JRM***", "mdist1:" + mdist1 + " | mdist2:" + mdist2 + " | dbdist1:" + dbdist1 + " | dbdist2:" + dbdist2 + " | dbdist3:" + dbdist3 + " | diff:" + difference);

                    if (((dbdist2 - dbdist3) <= 0.5) && ((dbdist2 - dbdist3) > 0.0))
                    {
                        preZoneDetection(i);
                    }

                    if (mdist1 == 0) {
                        zone = routePointsList.get(i).getZone();
                        count++;
                        Log.i("***JRM***", "i:" + i + " zone: " + zone + " count: " + count);
                        setCurrentZone(zone);
                        //curPoint = i;
                        //j = 0;
                        onroute = true;
                        /*timerFindNextZone.cancel();
                        timerFindNextZone.purge();
                            *//*if (i == routePointsList.size() - 3) {*//*
                                *//*forwardJourney = false;
                                returnJourney = true;*//*
                            *//*} else {
                                //findDirection();
                            }*//*
                        jrmTimerCheck();*/
                        jrmFun = 2;
                    } else if (mdist2 == 0) {
                        if(i < routePointsList.size()-3) {
                            i++;
                            zone = routePointsList.get(i).getZone();
                            count++;
                            Log.i("***JRM***", "i:" + i + " zone: " + zone + " count: " + count);
                            setCurrentZone(zone);
                            //curPoint = i;
                            //j = 0;
                            onroute = true;
                            /*timerFindNextZone.cancel();
                            timerFindNextZone.purge();
                            *//*if (i == routePointsList.size() - 3) {*//*
                                *//*forwardJourney = false;
                                returnJourney = true;*//*
                            *//*} else {
                                //findDirection();
                            }*//*
                            jrmTimerCheck();*/
                            jrmFun = 2;
                        }
                        else
                        {
                            /*timerFindNextZone.cancel();
                            timerFindNextZone.purge();
                            //curPoint = i;
                                *//*forwardJourney = false;
                                returnJourney = true;*//*
                            jrmTimer();*/
                            jrmFun = 0;
                        }
                    } else if ((dbdist3 > dbdist1) && (dbdist3 < dbdist2) && (mdist2 <= difference)) {
                        zone = routePointsList.get(i).getZone();
                        count++;
                        Log.i("***JRM***", "i:" + i + " zone: " + zone + " count: " + count);
                        setCurrentZone(zone);
                        //curPoint = i;
                        j = 0;
                        onroute = true;
                        //checkZoneContinuously(locationA, locationB, routePointsList, curPoint);
                        //break;
                        /*timerFindNextZone.cancel();
                        timerFindNextZone.purge();
                            *//*if (i == routePointsList.size() - 2) {
                                forwardJourney = false;
                                returnJourney = true;
                            } else {*//*
                        //findDirection();
                            *//*}*//*
                        jrmTimerCheck();*/
                        jrmFun = 2;
                    } else {
                        if ((i - curPoint) == 0)
                        {
                            jrmCheckDeviation();
                        }
                        if ((i - curPoint < 10) && (i < routePointsList.size() - 3)) {
                            i++;
                            //findDirection();
                            Log.i("***JRM***", "else i++");
                        } else {
                            Log.i("***JRM***", "else inside else j++");
                            i = curPoint;

                            j++;

                            //findDirection();
                        }
                        //onroute = false;
                        //continue;
                    }
                } else if (j == 20) {
                    //jrmCheckDeviation();
                    onroute = false;
                }

            } else {
                //i = curPoint;
                /*timerFindNextZone.cancel();
                timerFindNextZone.purge();*/
                ///Log.i("***JRMRoute***", "Deviation");
                //jrmCheckDeviation();
                i = 0;
                /*jrmTimer();*/
                jrmFun = 0;
            }
        }
    }

    private void checkZone()
    {
        myLocation.setLatitude(globalVariable.getLatitude());
        myLocation.setLongitude(globalVariable.getLongitude());

        //dbdist1 = routePointsList.get(i).getDist();
        //dbdist2 = routePointsList.get(i + 1).getDist();
        if (returnJourney == true) {

            locationA.setLatitude(routePointsList.get(i+1).getLat());
            locationA.setLongitude(routePointsList.get(i+1).getLng());
            //locationB = new Location("");
            locationB.setLatitude(routePointsList.get(i).getLat());
            locationB.setLongitude(routePointsList.get(i).getLng());

            mdist1 = calculateDistance(locationA, myLocation);
            mdist2 = calculateDistance(myLocation, locationB);
            dbdist1 = routePointsList.get(i+1).getDist();
            dbdist2 = routePointsList.get(i).getDist();
            dbdist3 = dbdist1-mdist1;
            difference = dbdist1-dbdist2;
                /*if (difference < 0 && dbdist2 == 0) {
                    difference = difference * (-1);
                    dbdist2 = dbdist3 + 1;
                }*/
            Log.i("***JRM***", "***JRMCheckZoneReturn***");
            Log.i("***JRM***", "MyLoc Lat:" + myLocation.getLatitude() + " | Long:" + myLocation.getLongitude());
            /*Log.i("***JRM***", "LocA Lat:" + locationA.getLatitude() + " | Long:" + locationA.getLongitude());
            Log.i("***JRM***", "LocB Lat:" + locationB.getLatitude() + " | Long:" + locationB.getLongitude());*/
            Log.i("***JRM***", "mdist1:" + mdist1 + " | mdist2:" + mdist2 + " | dbdist1:" + dbdist1 + " | dbdist2:" + dbdist2 + " | dbdist3:" + dbdist3 + " | diff:" + difference);
            if (mdist1 == 0) {
                Log.i("***JRM***","in mdist==1");

                if(i < routePointsList.size()-3) {
                    i++;
                    zone = routePointsList.get(i).getZone();
                    count++;
                    Log.i("***JRM***", "i:" + i + " zone: " + zone + " count: " + count);
                    setCurrentZone(zone);
                    //curPoint = i;
                }
                //findDirection();
            } else if (mdist2 == 0) {
                //if(i > 0) {
                //i--;
                        /*locationA.setLatitude(routePointsList.get(i).getLat());
                        locationA.setLongitude(routePointsList.get(i).getLng());
                        locationB.setLatitude(routePointsList.get(i + 1).getLat());
                        locationB.setLongitude(routePointsList.get(i + 1).getLng());
                        dbdist1 = routePointsList.get(i).getDist();
                        dbdist2 = routePointsList.get(i + 1).getDist();*/
                        /*zone = routePointsList.get(i).getZone();
                        count++;
                        Log.i("***JRM***", "i:" + i + " zone: " + zone + " count: " + count);
                        setCurrentZone(zone);
                        curPoint = i;*/
                //findDirection();
                //}
                //else
                //{
                //timerCheckZone.cancel();
                //timerCheckZone.purge();
                //curPoint = i;
                //forwardJourney = true;
                //returnJourney = false;
                //jrmTimer();
                //}

            } else if ((dbdist3 > dbdist2) && (dbdist3 < dbdist1) && (mdist2 <= difference)) {
                    /*if (i <= 0) {
                        forwardJourney = true;
                        returnJourney = false;
                        //timerCheckZone.cancel();
                        //timerCheckZone.purge();
                        //i = routePointsList.size()-;
                        //curPoint = i;
                        //jrmTimer();
                    } else {
                        //findDirection();
                    }*/

                if (((dbdist3 - dbdist2) <= 0.5) && ((dbdist3 - dbdist2) > 0.0))
                {
                    preZoneDetection(i);
                }

            } else {
                Log.i("***JRM***","in check zone else return");
                /*timerCheckZone.cancel();
                timerCheckZone.purge();*/
                i--;
                //findDirection();
                /*jrmTimerFindNext();*/
                jrmFun = 1;
            }

        } else { //forwardJourney==true

            locationA.setLatitude(routePointsList.get(i).getLat());
            locationA.setLongitude(routePointsList.get(i).getLng());
            //locationB = new Location("");
            locationB.setLatitude(routePointsList.get(i + 1).getLat());
            locationB.setLongitude(routePointsList.get(i + 1).getLng());

            mdist1 = calculateDistance(locationA, myLocation);
            mdist2 = calculateDistance(myLocation, locationB);
            dbdist1 = routePointsList.get(i).getDist();
            dbdist2 = routePointsList.get(i + 1).getDist();

            dbdist3 = mdist1 + dbdist1;
            difference = dbdist2 - dbdist1;
                /*if (difference < 0 && dbdist2 == 0) {
                    difference = difference * (-1);
                    dbdist2 = dbdist3 + 1;
                }*/
            Log.i("***JRM***", "***JRMCheckZone***");
            Log.i("***JRM***", "MyLoc Lat:" + myLocation.getLatitude() + " | Long:" + myLocation.getLongitude());
            /*Log.i("***JRM***", "LocA Lat:" + locationA.getLatitude() + " | Long:" + locationA.getLongitude());
            Log.i("***JRM***", "LocB Lat:" + locationB.getLatitude() + " | Long:" + locationB.getLongitude());*/
            Log.i("***JRM***", "mdist1:" + mdist1 + " | mdist2:" + mdist2 + " | dbdist1:" + dbdist1 + " | dbdist2:" + dbdist2 + " | dbdist3:" + dbdist3 + " | diff:" + difference);
            if (mdist1 == 0) {
                Log.i("***JRM***","in mdist1==0");
                //findDirection();
            } else if (mdist2 == 0) {
                if(i < routePointsList.size()-3) {
                    i++;
                        /*locationA.setLatitude(routePointsList.get(i).getLat());
                        locationA.setLongitude(routePointsList.get(i).getLng());
                        locationB.setLatitude(routePointsList.get(i + 1).getLat());
                        locationB.setLongitude(routePointsList.get(i + 1).getLng());
                        dbdist1 = routePointsList.get(i).getDist();
                        dbdist2 = routePointsList.get(i + 1).getDist();*/
                    zone = routePointsList.get(i).getZone();
                    count++;
                    Log.i("***JRM***", "i:" + i + " zone: " + zone + " count: " + count);
                    setCurrentZone(zone);
                    //curPoint = i;
                    //findDirection();
                }
                else
                {
                        /*timerCheckZone.cancel();
                        timerCheckZone.purge();
                        //curPoint = i;
                        forwardJourney = false;
                        returnJourney = true;
                        jrmTimer();*/
                }

            } else if ((dbdist3 > dbdist1) && (dbdist3 < dbdist2) && (mdist2 <= difference)) {
                    /*if (i >= routePointsList.size() - 3) {
                        forwardJourney = false;
                        returnJourney = true;
                        timerCheckZone.cancel();
                        timerCheckZone.purge();
                        //i = routePointsList.size()-;
                        //curPoint = i;
                        jrmTimer();
                    } else {
                        //findDirection();
                    }*/

                if (((dbdist2 - dbdist3) <= 0.5) && ((dbdist2 - dbdist3) > 0.0))
                {
                    preZoneDetection(i);
                }

            } else {
                Log.i("***JRM***","in check zone else");
                /*timerCheckZone.cancel();
                timerCheckZone.purge();*/
                i++;
                //findDirection();
                /*jrmTimerFindNext();*/
                jrmFun = 1;
            }
        }
    }

    /*private Handler handlerFindNextZone = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            //super.handleMessage(msg);

        }
    };

    private Handler handlerCheckZone = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            //super.handleMessage(msg);

        }
    };*/

    private void setCurrentZone(String z) {
        Log.i("***JRM***", "zone: " + z);

        zone_detection = true;
        zone_dv = false;
        pre_zone = false;
        globalVariable.setZone(z);
        globalVariable.setPointID(String.valueOf(routePointsList.get(i).getZoneid()));
        intentZoneUpdate = new Intent(zoneAction);
        intentZoneUpdate.putExtra("zone", z);
        intentZoneUpdate.putExtra("pid", "" + routePointsList.get(i).getZoneid());
        //sendBroadcast(intentZoneUpdate);
        LocalBroadcastManager.getInstance(myContext).sendBroadcast(intentZoneUpdate);
        if(i < curPoint)
        {
            forwardJourney = false;
            returnJourney = true;
        }
        else
        {
            forwardJourney = true;
            returnJourney = false;
        }
        curPoint = i;
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
        if(z.equals("R"))
        {
            mediaPlayer = MediaPlayer.create(this, R.raw.in_red_zone_speed_limit_30);
            mediaPlayer.start();

                globalVariable.setOsRed(true);
                globalVariable.setOsYellow(false);
        }
        else if (z.equals("G"))
        {
            mediaPlayer = MediaPlayer.create(this, R.raw.in_green_zone_speed_limit_65);
            mediaPlayer.start();

                globalVariable.setOsRed(false);
                globalVariable.setOsYellow(false);
        }
        else if (z.equals("Y"))
        {
            mediaPlayer = MediaPlayer.create(this, R.raw.in_yellow_zone_speed_limit_40);
            mediaPlayer.start();

                globalVariable.setOsRed(false);
                globalVariable.setOsYellow(true);
        }
        new MyLogger().storeMassage("***JRM***","zone:"+z+" | pointid:"+routePointsList.get(i).getZoneid());

    }

    private void jrmCheckDeviation()
    {
        if (returnJourney == true)
        {
            if(zone_detection == true)
            {
                if (zone_dv == true)
                {
                }
                else if (zone_dv == false)
                {
                    jrmZoneDeviation();
                }
            }
            else if (zone_detection == false)
            {
                globalVariable.setOsRed(false);
                globalVariable.setOsYellow(false);
            }
            else
            {
                globalVariable.setOsRed(false);
                globalVariable.setOsYellow(false);
            }
        }
        else if (forwardJourney == true)
        {
            if (zone_detection == true)
            {
                if (zone_dv == true)
                {}
                else if (zone_dv == false)
                {
                    jrmZoneDeviation();
                }
            }
            else if (zone_detection == false)
            {
                globalVariable.setOsRed(false);
                globalVariable.setOsYellow(false);
            }
            else
            {
                globalVariable.setOsRed(false);
                globalVariable.setOsYellow(false);
            }
        }
    }

    private void jrmZoneDeviation()
    {
        double mdist11, mdist22, mdist33;
        double area, s, jk;

        mdist11 = mdist1 * 1000;
        mdist22 = mdist2 * 1000;
        mdist33 = difference * 1000;
        s = (double) ((mdist11 + mdist22 + mdist33)/2);
        jk = (double) (s * (s-mdist11) * (s-mdist22) * (s-mdist33));
        area = Math.sqrt(jk);
        int height = (int)((2 * area)/ mdist33);

        if (height > 500 && globalVariable.getSpeed() > 3)
        {
            jrmDeviationStamp();
        }
    }

    private void jrmDeviationStamp()
    {
        if (zone_dv == false)
        {
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
            mediaPlayer = MediaPlayer.create(this, R.raw.route_deviation);
            mediaPlayer.start();
        }
        zone_dv = true;
        globalVariable.setDvFlag(true);
        globalVariable.setDvDateTime(globalVariable.getDateTime());
        globalVariable.setDvGpsAV(globalVariable.isGpsAV());
        globalVariable.setOsRed(false);
        globalVariable.setOsYellow(false);
        globalVariable.setPointID("");

        Intent intent = new Intent(zoneAction);
        intent.putExtra("zone", "dv");
        intent.putExtra("pid", "");
        LocalBroadcastManager.getInstance(myContext).sendBroadcast(intent);

        Log.i("***JRM***","Route Deviation | DeviationZone: "+routePointsList.get(curPoint).getZoneid());
        new MyLogger().storeMassage("***JRM***","Route Deviation | DeviationZone: "+routePointsList.get(curPoint).getZoneid());
    }

    private void preZoneDetection(int nextZone)
    {
        if (zone_detection == true && pre_zone == false)
        {
            if (!(globalVariable.getZone().equals(routePointsList.get(nextZone).getZone())))
            {
                Log.i("***JRM***", "Pre-zone detection: "+routePointsList.get(nextZone).getZone());
                new MyLogger().storeMassage("***JRM***", "Pre-zone detection: "+routePointsList.get(nextZone).getZone());
                pre_zone = true;
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
                if (routePointsList.get(nextZone).getZone().equals("R"))
                {
                    mediaPlayer.create(this, R.raw.entering_into_red_zone);
                    mediaPlayer.start();
                }
                else if (routePointsList.get(nextZone).getZone().equals("Y"))
                {
                    mediaPlayer.create(this, R.raw.entering_into_yellow_zone);
                    mediaPlayer.start();
                }
                else if (routePointsList.get(nextZone).getZone().equals("G"))
                {
                    mediaPlayer.create(this, R.raw.entering_into_green_zone);
                    mediaPlayer.start();
                }
            }
        }
    }

    /*private void findDirection() {
        if (comp == 0) {
            prevDist = dbdist3;
            comp++;
        }
        if (prevDist > dbdist3) {
            comp = 0;
            forwardJourney = false;
            returnJourney = true;
        } else if (prevDist < dbdist3) {
            comp = 0;
            forwardJourney = true;
            returnJourney = false;
        } else if (prevDist == dbdist3) {

        } else {
            forwardJourney = false;
            returnJourney = false;
        }
        Log.i("***JRM***","in findDir: fwd: "+forwardJourney+" ret: "+returnJourney);
    }*/

    /*
     * For to check whether route exists or not. If not then download.
     * */
    void checkAndDownloadRoute(String routeName) {
        String sub = routeName;
        //String filename = sub + ".txt";

        SQLiteDatabase db = myContext.openOrCreateDatabase("Config", myContext.MODE_PRIVATE, null);

        String host = retrive(db, "hostRoute");
        String port = retrive(db, "portRoute");
        String userName = retrive(db, "unRoute");
        String password = retrive(db, "pwRoute");

        db.close();

        /*String host = "pop.mobile-eye.in";
        String port = "995";
        String userName = "jrmroute@mobile-eye.in";
        String password = "jrmroute@123";*/
        /*String userName = "8991@mobile-eye.in";
        String password = "transworld@123";*/

        SQLiteDatabase database = myContext.openOrCreateDatabase("JRMRoute.db", myContext.MODE_PRIVATE, null);
        Cursor cursor = database.rawQuery("select name from sqlite_master where type='table' and name='" + sub + "'", null);
        String isTable = "no";
        if (cursor != null) {
            if (cursor.moveToNext())
                isTable = cursor.getString(0);
        }
        if (!(isTable.equals(sub))) { // first this block will be called becoz routtable not exist
            //Toast.makeText(JRMPanelActivity.this, "Route not exists.", Toast.LENGTH_SHORT).show();
            Log.i("jrm-----", "Table not found. Downloading...");
            new MailAction().execute(host, port, userName, password, sub);
        }
        else if(isTable.equals(sub)) { // after save route file in local database , this block will execute

            //Toast.makeText(JRMPanelActivity.this, "Route already exists.", Toast.LENGTH_SHORT).show();
            //readTable(sub);
            Log.i("jrm-----", "Table exists");
            routePointsList = readPoints(routeNo);

            jrmTimer();
        }
        else
        {
            Log.i("jrm-----", "Route not found");
        }
        cursor.close();
        database.close();
    }

    private class MailAction extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {

            String host = (String) objects[0];
            String port = (String) objects[1];
            String userName = (String) objects[2];
            String password = (String) objects[3];
            String sub = (String) objects[4];
            //Log.i("***EmailAttach***","sub : "+sub);
            boolean f = emailAttachmentReceiver(host, port, userName, password, sub);

            return f;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            boolean f = (boolean) o;
            if (f) {
                Log.i("***EmailAttach***", "Found");
                routePointsList = readPoints(routeNo);

                jrmTimer();
                //readTable(txtFilename.getText().toString());
            } else {
                Log.i("***EmailAttach***", "Not found");
                Log.i("jrm-----", "Route not found");
                //Toast.makeText(myContext, "Not found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean emailAttachmentReceiver(String host, String port,
                                           String userName, String password, String sub) {

        boolean found = false;
        String attachFiles = "";

        Properties properties = new Properties();

        // server setting
        properties.put("mail.pop3.host", host);
        properties.put("mail.pop3.port", port);

        // SSL setting
        properties.setProperty("mail.pop3.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.pop3.socketFactory.fallback", "false");
        properties.setProperty("mail.pop3.socketFactory.port",
                String.valueOf(port));

        Session session = Session.getInstance(properties);

        try {
            // connects to the message store
            Store store = session.getStore("pop3");
            store.connect(userName, password); // do the connection

            // opens the inbox folder
            Folder folderInbox = store.getFolder("INBOX");
            folderInbox.open(Folder.READ_ONLY);

            // fetches new messages from server
            Message[] arrayMessages = folderInbox.getMessages();

            for (int i = (arrayMessages.length - 1); i >= 0; i--) {
                Message message = arrayMessages[i];
                Address[] fromAddress = message.getFrom();
                String from = fromAddress[0].toString();
                String subject = message.getSubject();
                String sentDate = message.getSentDate().toString();

                //
                /*System.out.println("Message #" + (i + 1) + ":");
                System.out.println("\t From: " + from);
                System.out.println("\t Subject: " + subject);
                System.out.println("\t Sent Date: " + sentDate);
                System.out.println("\t Contents: " + message.getContent());*/

                if (subject.equals(sub)) {
                    String contentType = message.getContentType();

                    if (contentType.contains("multipart")) {
                        // content may contain attachments
                        Multipart multiPart = (Multipart) message.getContent();
                        int numberOfParts = multiPart.getCount();
                        ///Log.i("***EmailAttach***","inside if");
                        for (int partCount = 0; partCount < numberOfParts; partCount++) {
                            MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                            ///Log.i("***EmailAttach***","inside for");
                            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                                // this part is attachment
                                ///Log.i("***Emai+03lAttach***","inside inner if");
                                String fileName = part.getFileName(); // getting the attachement
                                attachFiles = fileName;
                                found = true;

                                InputStream inputStream = part.getInputStream(); // InputStream is used to read data from a source
                                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                                //FileOutputStream fout = openFileOutput(attachFiles, MODE_PRIVATE);
                                String fileContents = "";
                                //String f="";
                                int c = 0, d = 0;
                                Log.i("***EmailAttach***", "file:" + fileName);

                                String data;
                                while ((data = br.readLine()) != null) { //finally getting the data from the attachement
                                    fileContents = fileContents + data;
                                    d++;
                                }
                                Log.i("***EmailAttach***", "" + c + " , " + d);

                                /*fout.write(fileContents.getBytes());
                                fout.close();*/
                                // this class allows you to break a string into tokens. It is simple way to break string.
                                StringTokenizer st = new StringTokenizer(fileContents, "#D:");
                                SQLiteDatabase database = myContext.openOrCreateDatabase("JRMRoute.db", myContext.MODE_PRIVATE, null);
                                database.execSQL("create table " + sub + "(pno INTEGER PRIMARY KEY AUTOINCREMENT,lat VARCHAR2(20), long VARCHAR2(20), dist VARCHAR2(10), zone VARCHAR2(1), zoneid number(10))");
                                while (st.hasMoreTokens()) {
                                    String s = st.nextToken();
                                    //System.out.println(s + " = " + (s.endsWith("$") || s.endsWith("@") ? s.substring(0,s.length()-1):s));
                                    System.out.print(s + " = ");
                                    if (s.endsWith("$") || s.endsWith("@"))  // removing $ sign from string
                                        s = s.substring(0, s.length() - 1);
                                    System.out.println(s);
                                    StringTokenizer st2 = new StringTokenizer(s, ",");
                                    String zone[] = new String[5];
                                    int j = 0;
                                    while (st2.hasMoreTokens()) {
                                        zone[j] = st2.nextToken();
                                        System.out.print(zone[j] + " | ");
                                        j++;
                                    }
                                    j = Integer.parseInt(zone[4]);
                                    database.execSQL("insert into " + sub + "(lat,long,dist,zone,zoneid) values('" + zone[0] + "','" + zone[1] + "','" + zone[2] + "','" + zone[3] + "'," + j + ")");
                                    System.out.println();
                                    globalVariable.setJdStamp(true);
                                    globalVariable.setJdDateTime(globalVariable.getDateTime());
                                    globalVariable.setJdGpsAV(globalVariable.isGpsAV());
                                }
                                Log.i("***EmailAttach***", "File saved");

                            }
                        }

                    }
                    // print out details of each message
                    System.out.println("Message #" + (i + 1) + ":");
                    System.out.println("\t From: " + from);
                    System.out.println("\t Subject: " + subject);
                    System.out.println("\t Sent Date: " + sentDate);
                    //System.out.println("\t Message: " + messageContent);
                    System.out.println("\t Attachments: " + attachFiles);
                    break;
                }
            }

            // disconnect
            folderInbox.close(false);
            store.close();
            Log.i("***EmailAttachment***", "Completed");

        } catch (NoSuchProviderException ex) {
            System.out.println("No provider for pop3.");
            ex.printStackTrace();
        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store");
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return found;
    }

    public ArrayList<RoutePoint> readPoints(String tbname) {
        SQLiteDatabase database = myContext.openOrCreateDatabase("JRMRoute.db", myContext.MODE_PRIVATE, null);
        RoutePoint routePoint = null;
        ArrayList<RoutePoint> routePoints = null;
        try {
            Cursor cursor = database.rawQuery("select * from " + tbname, null);
            routePoints = new ArrayList<>();
            cursor.moveToNext();
            while (cursor.moveToNext()) {
                routePoint = new RoutePoint();
                routePoint.setLat(revConvertToStandard(Double.parseDouble(cursor.getString(cursor.getColumnIndex("lat")))));
                routePoint.setLng(revConvertToStandard(Double.parseDouble(cursor.getString(cursor.getColumnIndex("long")))));
                routePoint.setDist(Double.parseDouble(cursor.getString(cursor.getColumnIndex("dist"))));
                routePoint.setZone(cursor.getString(cursor.getColumnIndex("zone")));
                routePoint.setZoneid(cursor.getInt(cursor.getColumnIndex("zoneid")));
                //Log.i("JRMRoute-----",""+cursor.getInt(cursor.getColumnIndex("zoneid")));
                routePoints.add(routePoint);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("***JRMRoute***", "Error in SQL");
        }
        database.close();
        return routePoints;
    }

    public float calculateDistance(Location srcLocation, Location destLocation) {

        float distance = srcLocation.distanceTo(destLocation);//in meters
        distance = distance / 1000;// convert it to kilometer
        DecimalFormat decimalFormat = new DecimalFormat(".00");
        //Log.i("jrm-----dist",""+Float.parseFloat(decimalFormat.format(distance)));
        return Float.parseFloat(decimalFormat.format(distance));
    }

    public double revConvertToStandard(double x) {
        float convertedVal;
        double v = x / 100;//Log.i("jrm-----vv",""+v);
        long v1 = (long) (v);//Log.i("jrm-----v1",""+v1);
        float v2 = (float) (v - v1);//Log.i("jrm-----v2",""+v2);
        float v3 = (v2 * 100) / 60;//Log.i("jrm-----v3",""+v3);
        convertedVal = v1 + v3;//Log.i("jrm-----val",""+convertedVal);//18.5314
        DecimalFormat decimalFormat = new DecimalFormat(".000000");
        double d = Double.parseDouble(decimalFormat.format(convertedVal));
        //Log.i("jrm-----",""+d);
        return d;
    }


    void jrmTimer() {

        j = 0;
        timerDetectRoute = new Timer(); //Timer object is a single background thread that is used to execute all of the timer's tasks, sequentially in future
        timerTaskDetectRoute = new TimerTask() { // A task that can be scheduled for one-time or repeated execution by a Timer.
            @Override
            public void run() {
                handlerDetectRoute.sendEmptyMessage(0);
            }
        };
        timerDetectRoute.scheduleAtFixedRate(timerTaskDetectRoute, 0, 1000);
    }

    /*void jrmTimerFindNext() {
        j = 0;
        //i = curPoint;
        timerFindNextZone = new Timer();
        timerTaskFindNextZone = new TimerTask() {
            @Override
            public void run() {
                handlerFindNextZone.sendEmptyMessage(0);
            }
        };
        timerFindNextZone.scheduleAtFixedRate(timerTaskFindNextZone, 0, 1000);
    }

    void jrmTimerCheck() {
        //timerFindNextZone.cancel();
        //timerFindNextZone.purge();
        j = 0;
        timerCheckZone = new Timer();
        timerTaskCheckZone = new TimerTask() {
            //Location myLocation = new Location("");
            //float mdist1, mdist2;
            //double dbdist3,difference;
            @Override
            public void run() {
                handlerCheckZone.sendEmptyMessage(0);
            }
        };
        timerCheckZone.scheduleAtFixedRate(timerTaskCheckZone, 0, 1000);
    }*/

    private String retrive(SQLiteDatabase db,String p) {
        try {
            String res;
            Cursor c = db.rawQuery("select value from file where parameter='" + p + "'", null);
            c.moveToFirst();
            do {
                res = c.getString(0);
            } while (c.moveToNext());
            return res;
        } catch (Exception e) {
            return "error" + e.getMessage();
        }
    }
}