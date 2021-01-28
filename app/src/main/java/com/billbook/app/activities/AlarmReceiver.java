package com.billbook.app.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    private final String SOMEACTION = "com.marketexcel.marketexcelapp.setAlarm"; //packagename is com.whatever.www

    @Override
    public void onReceive(Context context, Intent intent) {
       /* Time now = new Time();
        now.setToNow();
        String time = FileHandler.timeFormat(now);

        String action = intent.getAction();
        if (SOMEACTION.equals(action)) {
            // here you call a service etc.
        }*/
    }
}