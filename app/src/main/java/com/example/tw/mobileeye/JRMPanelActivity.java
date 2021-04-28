package com.example.tw.mobileeye;


import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class JRMPanelActivity extends Fragment {

    Button btnStart, btnStop, btnUseSimulator,btnViewSimulator;
    ImageView imageView;
    TextView textView;
    JRMService jrmService;
    Intent serviceIntent;
    EditText txtRoute;
    UpdateZoneRec updateZoneReceiver;
    boolean isStart = false;
    TextView tvPointID;
    MediaPlayer mediaPlayer;
    GlobalVariable globalVariable;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.jrm_panel, container, false);

        globalVariable = (GlobalVariable) getActivity().getApplicationContext();
        /*btnStart = (Button) view.findViewById(R.id.btn1);
        btnStop = (Button) view.findViewById(R.id.btn2);
        txtRoute = (EditText) view.findViewById(R.id.txtRoute);*/
        imageView = (ImageView) view.findViewById(R.id.imageView);
        textView = (TextView) view.findViewById(R.id.textView);

        /*tvPointID = (TextView) view.findViewById(R.id.tvPointID);*/
        //btnUseSimulator = (Button) findViewById(R.id.btnUseSimulator);
        //btnViewSimulator = (Button) view.findViewById(R.id.btnViewSimulator);

        //imageView.setVisibility(View.INVISIBLE);
        /*btnStart.setEnabled(false);
        btnStop.setEnabled(false);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceIntent = new Intent(getActivity() ,JRMService.class);
                String routeNo = txtRoute.getText().toString();
                serviceIntent.putExtra("RouteNo", routeNo);
                getActivity().startService(serviceIntent);
                getActivity().bindService(serviceIntent,mConnection, Context.BIND_AUTO_CREATE);
                //isStart = true;
                //globalVariable.setJRMService(true);
                btnStop.setEnabled(true);
                btnStart.setEnabled(false);

                globalVariable.setJrmON(true);
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jrmService.stopJRM();
                getActivity().unbindService(mConnection);
                //isStart = false;
                //globalVariable.setJRMService(false);
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);

                globalVariable.setJrmON(false);
            }
        });*/

        /*IntentFilter intentFilter = new IntentFilter(JRMService.zoneAction);
        updateZoneReceiver = new UpdateZoneRec();
        //registerReceiver(updateZoneReceiver, intentFilter);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(updateZoneReceiver, intentFilter);*/

        updateZone(globalVariable.getZone());
        return view;
    }

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jrm_panel);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        btnStart = (Button) findViewById(R.id.btn1);
        btnStop = (Button) findViewById(R.id.btn2);
        txtRoute = (EditText) findViewById(R.id.txtRoute);
        imageView = (ImageView) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.textView);
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        tvPointID = (TextView) findViewById(R.id.tvPointID);
        //btnUseSimulator = (Button) findViewById(R.id.btnUseSimulator);
        btnViewSimulator = (Button) findViewById(R.id.btnViewSimulator);
        globalVariable = (GlobalVariable) getApplicationContext();

        //imageView.setVisibility(View.INVISIBLE);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceIntent = new Intent(JRMPanelActivity.this,JRMService.class);
                String routeNo = txtRoute.getText().toString();
                serviceIntent.putExtra("RouteNo", routeNo);
                startService(serviceIntent);
                bindService(serviceIntent,mConnection, Context.BIND_AUTO_CREATE);
                //isStart = true;
                btnStop.setEnabled(true);
                btnStart.setEnabled(false);
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jrmService.stopJRM();
                unbindService(mConnection);
                //isStart = false;
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
            }
        });

        *//*btnUseSimulator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btnUseSimulator.getText().toString().equals("Use Simulator"))
                {
                    globalVariable.setSimulator(true);
                    Intent i = new Intent(JRMPanelActivity.this, JRMSimulatorActivity.class);
                    startActivity(i);
                    btnUseSimulator.setText("Stop Simulator");
                }
                else
                {
                    globalVariable.setSimulator(false);
                    btnUseSimulator.setText("Use Simulator");
                }
            }
        });*//*

        btnViewSimulator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(JRMPanelActivity.this, JRMSimulatorActivity.class);
                startActivity(i);
            }
        });

    }*/
/*
    @Override
    public void onResume() {
        IntentFilter intentFilter = new IntentFilter(JRMService.zoneAction);
        updateZoneReceiver = new UpdateZoneRec();
        //registerReceiver(updateZoneReceiver, intentFilter);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(updateZoneReceiver, intentFilter);
        super.onResume();
    }*/

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(JRMService.zoneAction);
        updateZoneReceiver = new UpdateZoneRec();
        //registerReceiver(updateZoneReceiver, intentFilter);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(updateZoneReceiver, intentFilter);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            JRMService.LocalBinder localBinder = (JRMService.LocalBinder) iBinder;
            jrmService = localBinder.getServiceInstance();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    /*public class UpdateZoneReceiver extends BroadcastReceiver
    {
        public UpdateZoneReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            String zone = intent.getStringExtra("zone");
            updateZone(zone);
        }
    }*/
// This function updates JRM panel for zone color indication and speed limit
    private void updateZone(String zone) {
        imageView.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
        /*tvPointID.setVisibility(View.VISIBLE);*/
        switch (zone)
        {
            case "R":
                //imageView.setImageDrawable(getResources().getDrawable(R.drawable.icon_red));
                imageView.setImageResource(R.drawable.icon_red);
                textView.setText("30");
                /*tvPointID.setText("PID: " + globalVariable.getPointID());*/
                Log.i("***JRM***","red zone detected");
                /*if(mediaPlayer != null && mediaPlayer.isPlaying())
                {
                    mediaPlayer.stop();
                }
                mediaPlayer = MediaPlayer.create(this, R.raw.in_red_zone_speed_limit_30);
                mediaPlayer.start();*/
                Log.i("***JRM***","in_red_zone_speed_limit_30");
                break;
            case "G":
                //imageView.setImageDrawable(getResources().getDrawable(R.drawable.icon_green));

                Log.i("***JRM***","isadded:"+isAdded());
                //int imgres = getResources().getIdentifier("@drawable/icon_green", "drawable", getActivity().getPackageName());
                imageView.setImageResource(R.drawable.icon_green);
                textView.setText("65");
                /*tvPointID.setText("PID: " + globalVariable.getPointID());*/
                Log.i("***JRM***","green zone detected");
                /*if(mediaPlayer != null && mediaPlayer.isPlaying())
                {
                    mediaPlayer.stop();
                }
                mediaPlayer = MediaPlayer.create(this, R.raw.in_green_zone_speed_limit_65);
                mediaPlayer.start();*/
                Log.i("***JRM***","in_green_zone_speed_limit_65");
                break;
            case "Y":
                //imageView.setImageDrawable(getResources().getDrawable(R.drawable.icon_yellow));
                imageView.setImageResource(R.drawable.icon_yellow);
                textView.setText("40");
                /*tvPointID.setText("PID: " + globalVariable.getPointID());*/
                Log.i("***JRM***","yellow zone detected");
                /*if(mediaPlayer != null && mediaPlayer.isPlaying())
                {
                    mediaPlayer.stop();
                }
                mediaPlayer = MediaPlayer.create(this, R.raw.in_yellow_zone_speed_limit_40);
                mediaPlayer.start();*/
                Log.i("***JRM***","in_yellow_zone_speed_limit_40");
                break;
            case "dv":
                imageView.setImageResource(R.drawable.icon_yellow);
                textView.setText("40");
                /*tvPointID.setText("DvPID: " + globalVariable.getPointID());*/
                Log.i("***JRM***","route deviation");
                break;
            default:
                imageView.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.INVISIBLE);
                /*tvPointID.setVisibility(View.INVISIBLE);*/
                break;
        }
    }

    private class UpdateZoneRec extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            String zone = intent.getStringExtra("zone");

            /*if (zone.equals("stop"))
            {
                *//*getActivity().finish();*//*
                *//*Intent i = new Intent(getActivity(), MainActivity.class);
                getActivity().startActivity(i);*//*

                *//*Intent i = new Intent(getActivity(), MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().getBaseContext().startActivity(i);*//*

                imageView.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.INVISIBLE);
                tvPointID.setVisibility(View.INVISIBLE);
            }
            else */

            String pid = intent.getStringExtra("pid");
            globalVariable.setPointID(pid);
            //tvPointID.setText("PID: " + pid);
            updateZone(zone);

        }
    }
}