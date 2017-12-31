package com.backdoor.moove.core.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.backdoor.moove.core.async.DisableAsync;
import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.helper.DataBase;

public class JustBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new Thread(() -> {
            DataBase db = new DataBase(context);
            db.open();
            Cursor c = db.getReminders(Constants.ENABLE);
            if (c != null && c.moveToFirst()) {
                do {
                    long id = c.getLong(c.getColumnIndex(DataBase._ID));
                    long startTime = c.getLong(c.getColumnIndex(DataBase.START_TIME));
                    if (startTime > 0) {
                        new PositionDelayReceiver().setAlarm(context, id);
                    }
                } while (c.moveToNext());
            }
            if (c != null) c.close();
            db.close();
        }).start();

        new DisableAsync(context).execute();
    }
}
