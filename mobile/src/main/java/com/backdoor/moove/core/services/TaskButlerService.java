package com.backdoor.moove.core.services;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;

import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.helper.DataBase;

public class TaskButlerService extends IntentService {

    public TaskButlerService() {
        super("TaskButlerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //get all active reminders
        DataBase db = new DataBase(getApplicationContext());
        db.open();
        Cursor c = db.getReminders(Constants.ENABLE);
        if (c != null && c.moveToFirst()){
            do {
                long id = c.getLong(c.getColumnIndex(DataBase._ID));
                long startTime = c.getLong(c.getColumnIndex(DataBase.START_TIME));
                if (startTime > 0) {
                    new PositionDelayReceiver().setAlarm(getApplicationContext(), id);
                }
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        db.close();
        stopSelf();
    }
}