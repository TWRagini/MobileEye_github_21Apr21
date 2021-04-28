package com.example.tw.mobileeye;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

public class JRMTestingActivity extends AppCompatActivity {

    GlobalVariable globalVariable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jrmtesting);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //FragmentManager fragmentManager = getFragmentManager();
        //FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //JRMPanelFragment jrmPanelFragment = new JRMPanelFragment();
        //fragmentTransaction.replace(android.R.id.content, jrmPanelFragment);
        //JRMPanelActivity jrmPanelActivity = (JRMPanelActivity) fragmentManager.findFragmentById(R.id.JRMPanel);
        //fragmentTransaction.add(android.R.id.content, jrmPanelActivity);
        //fragmentTransaction.commit();

        globalVariable = (GlobalVariable) getApplicationContext();

        IntentFilter intentFilter = new IntentFilter(JRMService.jrmActivityAction);
        StopJRMActivity stopJRMActivity = new StopJRMActivity();
        LocalBroadcastManager.getInstance(this).registerReceiver(stopJRMActivity, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private class StopJRMActivity extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            globalVariable.setJrmStopCalled(true);
            Intent intentMain = new Intent(JRMTestingActivity.this, MainActivity.class);
            startActivity(intentMain);
            JRMTestingActivity.this.finish();
        }
    }
}