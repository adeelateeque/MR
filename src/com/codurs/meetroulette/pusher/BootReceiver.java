package com.codurs.meetroulette.pusher;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver
{
    AlarmManager am;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        setRepeatingAlarm(context);
    }

    public void setRepeatingAlarm(Context context)
    {
        Intent intent = new Intent(context, PushAlarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (5 * 60 * 1000), pendingIntent); // wake
    }

    public class PushAlarm extends BroadcastReceiver
    {
        NotificationManager nm;

        @Override
        public void onReceive(Context context, Intent intent)
        {
            Intent myIntent = new Intent(context, PusherService.class);
            context.startService(myIntent);
        }
    }
}