package com.example.tw.mobileeye;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootComplete extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        new MyLogger().storeMassage("BootComplete", "Mobile Turn ON");

        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            Intent serviceIntent = new Intent(context, AutoStartUp.class);
            context.startService(serviceIntent);
        }
    }

}