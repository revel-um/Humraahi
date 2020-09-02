package com.revel.humraahi.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.revel.humraahi.services.MyService;

public class CancelReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Looks like its time for a break", Toast.LENGTH_SHORT).show();
        context.stopService(new Intent(context, MyService.class));
    }
}
