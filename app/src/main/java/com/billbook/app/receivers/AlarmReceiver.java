package com.billbook.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.billbook.app.services.SyncService;
import com.billbook.app.utils.Constants;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "inside onReceiver intent.getAction()::" + intent.getAction());
        if (intent.getAction().equals(Constants.ALARM_RECEIVER_ACTION)) {
            // execute task
            context.startService(new Intent(context, SyncService.class));
        }
    }
}
