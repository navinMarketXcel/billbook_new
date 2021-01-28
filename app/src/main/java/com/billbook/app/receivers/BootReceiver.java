package com.billbook.app.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set alarms
            setAlarm(context);
        }
    }

    void setAlarm(Context context) {
        // get a Calendar object with current time
        Calendar cal = Calendar.getInstance();
        // add 5 minutes to the calendar object
        cal.add(Calendar.SECOND, 10);

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("com.marketexcel.marketexcelapp.AlarmReceiver");
        intent.putExtra("alarm_message", "O'Doyle Rules!");

        // In reality, you would want to have a static variable for the request code instead of 192837
        PendingIntent sender = PendingIntent.getBroadcast(context, 192837, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the AlarmManager service
        AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

        // every day at scheduled time
        Calendar calendar = Calendar.getInstance();
        // if it's after or equal 6 am schedule for next day
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 6) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);

    }
}