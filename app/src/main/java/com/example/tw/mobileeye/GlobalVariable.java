package com.example.tw.mobileeye;

import android.app.Application;
import android.util.Log;

import java.text.DecimalFormat;

/**
 * Created by tw on 8/23/2016.
 */
public class GlobalVariable extends Application {

    private double latitude;
    private double longitude;
    private String PointID;
    private String routeNo, zone="A";
    //private boolean isJRMService;
    private boolean osRed;
    private boolean osYellow;
    private boolean mailSendingFlag;
    private boolean JrmON;
    private boolean tfFlag;
    private String rmcString;
    private boolean jdStamp;
    private int speed;
    private String dateTime;
    private String jdDateTime;
    private String dvDateTime;
    private boolean dvFlag;
    private boolean gpsAV;
    private boolean dvGpsAV;
    private boolean jdGpsAV;
    private boolean sendnigFlage;
    private boolean jrmStopCalled;

    public boolean isJrmStopCalled() {
        return jrmStopCalled;
    }

    public void setJrmStopCalled(boolean jrmStopCalled) {
        this.jrmStopCalled = jrmStopCalled;
    }

    public boolean isSendnigFlage() {
        return sendnigFlage;
    }

    public void setSendnigFlage(boolean sendnigFlage) {
        this.sendnigFlage = sendnigFlage;
    }

    public boolean isDvGpsAV() {
        return dvGpsAV;
    }

    public void setDvGpsAV(boolean dvGpsAV) {
        this.dvGpsAV = dvGpsAV;
    }

    public boolean isJdGpsAV() {
        return jdGpsAV;
    }

    public void setJdGpsAV(boolean jdGpsAV) {
        this.jdGpsAV = jdGpsAV;
    }

    public boolean isGpsAV() {
        return gpsAV;
    }

    public void setGpsAV(boolean gpsAV) {
        this.gpsAV = gpsAV;
    }

    public boolean isDvFlag() {
        return dvFlag;
    }

    public void setDvFlag(boolean dvFlag) {
        this.dvFlag = dvFlag;
    }

    public String getJdDateTime() {
        return jdDateTime;
    }

    public void setJdDateTime(String jdDateTime) {
        this.jdDateTime = jdDateTime;
    }

    public String getDvDateTime() {
        return dvDateTime;
    }

    public void setDvDateTime(String dvDateTime) {
        this.dvDateTime = dvDateTime;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }


    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setRmcString(String rmc){
        rmcString = rmc;
        Log.e("setRmcString", rmcString);
    }

    public String getRmcString() {
        Log.e("getRmcString", ""+rmcString);
        return rmcString;
    }

    public boolean isJdStamp() {
        return jdStamp;
    }

    public void setJdStamp(boolean JdStamp1) {
        this.jdStamp = JdStamp1;
    }

    public boolean isTFSending() {
        return tfFlag;
    }

    public void setTFSendingFlage(boolean tfSendFlag) {
        this.tfFlag = tfSendFlag;
    }

    public boolean isMailSending() {
        return mailSendingFlag;
    }

    public void setMailSendingFlage(boolean SendingFlag) {
        this.mailSendingFlag = SendingFlag;
    }

    public boolean isJrmON() {
        return JrmON;
    }

    public void setJrmON(boolean JrmOn) {
        this.JrmON = JrmOn;
    }

    public boolean isOsRed() {
        return osRed;
    }

    public void setOsRed(boolean osRed) {
        this.osRed = osRed;
    }

    public boolean isOsYellow() {
        return osYellow;
    }

    public void setOsYellow(boolean osYellow) {
        this.osYellow = osYellow;
    }

    public double getLatitude() {
        //Log.e("GlobalVariable", "getLatitude "+latitude);
        return latitude;
    }
    public void setLatitude(double lat){
        latitude = revConvertToStandard(lat);
        //latitude = lat;
        //Log.e("GlobalVariable", "setLatitude "+latitude);
    }

    public double getLongitude() {
        //Log.e("GlobalVariable", "getLongitude "+longitude);
        return longitude;
    }
    public void setLongitude(double lon){
        longitude = revConvertToStandard(lon);
        //longitude = lon;
        //Log.e("GlobalVariable", "setLongitude "+longitude);
    }

    public String getPointID() {
        //Log.e("GlobalVariable", "getPointID "+PointID);
        return PointID;
    }
    public void setPointID(String pId){
        //latitude = revConvertToStandard(lat);
        PointID = pId;
        //Log.e("GlobalVariable", "setPointID "+PointID);
    }

    public String getRouteNo() {
        return routeNo;
    }

    public void setRouteNo(String routeNo) {
        this.routeNo = routeNo;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
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

}