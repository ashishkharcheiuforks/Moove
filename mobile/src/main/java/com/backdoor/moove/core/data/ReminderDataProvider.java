package com.backdoor.moove.core.data;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;

import com.backdoor.moove.core.helper.DataBase;

import java.util.ArrayList;

public class ReminderDataProvider {

    public static ArrayList<MarkerModel> getListData(Context context) {
        ArrayList<MarkerModel> list = new ArrayList<>();
        list.clear();
        DataBase db = new DataBase(context);
        db.open();
        Cursor c = db.getAllReminders();
        if (c != null && c.moveToFirst()) {
            do {
                String title = c.getString(c.getColumnIndex(DataBase.SUMMARY));
                long id = c.getLong(c.getColumnIndex(DataBase._ID));
                int icon = c.getInt(c.getColumnIndex(DataBase.MARKER));

                if (icon == -1) {
                    icon = 0;
                }

                list.add(new MarkerModel(title, id, icon));
            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }

        db.close();
        return list;
    }

    public static ArrayList<ReminderModel> load(Context context) {
        ArrayList<ReminderModel> list = new ArrayList<>();
        DataBase db = new DataBase(context);
        db.open();
        Cursor c = db.getAllReminders();
        if (c != null && c.moveToFirst()) {
            do {
                String title = c.getString(c.getColumnIndex(DataBase.SUMMARY));
                String type = c.getString(c.getColumnIndex(DataBase.TYPE));
                String number = c.getString(c.getColumnIndex(DataBase.NUMBER));
                String uuID = c.getString(c.getColumnIndex(DataBase.UUID));
                String melody = c.getString(c.getColumnIndex(DataBase.MELODY));
                long startTime = c.getLong(c.getColumnIndex(DataBase.START_TIME));
                long id = c.getLong(c.getColumnIndex(DataBase._ID));
                int statusDb = c.getInt(c.getColumnIndex(DataBase.STATUS_DB));
                int statusList = c.getInt(c.getColumnIndex(DataBase.STATUS_REMINDER));
                double lat = c.getDouble(c.getColumnIndex(DataBase.LATITUDE));
                double lon = c.getDouble(c.getColumnIndex(DataBase.LONGITUDE));
                int radius = c.getInt(c.getColumnIndex(DataBase.RADIUS));

                list.add(new ReminderModel(title, type, uuID, statusDb, startTime, id,
                        new double[]{lat, lon}, number, statusList, radius, melody));
            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }

        db.close();
        return list;
    }

    @Nullable
    public static ReminderModel getItem(Context mContext, long id) {
        ReminderModel item = null;
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()) {
            String title = c.getString(c.getColumnIndex(DataBase.SUMMARY));
            String type = c.getString(c.getColumnIndex(DataBase.TYPE));
            String number = c.getString(c.getColumnIndex(DataBase.NUMBER));
            String uuID = c.getString(c.getColumnIndex(DataBase.UUID));
            String melody = c.getString(c.getColumnIndex(DataBase.MELODY));
            long startTime = c.getLong(c.getColumnIndex(DataBase.START_TIME));
            int statusDb = c.getInt(c.getColumnIndex(DataBase.STATUS_DB));
            int statusList = c.getInt(c.getColumnIndex(DataBase.STATUS_REMINDER));
            double lat = c.getDouble(c.getColumnIndex(DataBase.LATITUDE));
            double lon = c.getDouble(c.getColumnIndex(DataBase.LONGITUDE));
            int radius = c.getInt(c.getColumnIndex(DataBase.RADIUS));

            item = new ReminderModel(title, type, uuID, statusDb, startTime, id,
                    new double[]{lat, lon}, number, statusList, radius, melody);
        }
        if (c != null) {
            c.close();
        }

        db.close();
        return item;
    }
}
