package com.backdoor.moove.core.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.backdoor.moove.core.helper.DataBase;
import com.backdoor.moove.core.utils.SuperUtil;

import java.util.Calendar;

public class PositionDelayReceiver extends BroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!SuperUtil.isServiceRunning(context, GeolocationService.class)) {
            context.startService(new Intent(context, GeolocationService.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    public void setAlarm(Context context, long id) {
        DataBase db = new DataBase(context);
        db.open();
        Cursor c = db.getReminder(id);

        Integer i = (int) (long) id;
        long startTime = 0;
        if (c != null && c.moveToNext()) {
            startTime = c.getLong(c.getColumnIndex(DataBase.START_TIME));
        }
        if (c != null) c.close();
        Intent intent = new Intent(context, PositionDelayReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, 0);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
        db.close();
    }

    public void cancelAlarm(Context context, long id) {
        Integer i = (int) (long) id;
        Intent intent = new Intent(context, PositionDelayReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, i, intent, 0);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
    }
}