package com.backdoor.moove.core.data;

import android.content.Context;
import android.database.Cursor;

import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.helper.Coloring;
import com.backdoor.moove.core.helper.DataBase;

import java.util.ArrayList;
import java.util.List;

public class ReminderDataProvider {
    private List<ReminderModel> data;
    private Context mContext;
    private ReminderModel mLastRemovedData;
    private int mLastRemovedPosition = -1;

    public ReminderDataProvider(Context mContext){
        data = new ArrayList<>();
        this.mContext = mContext;
        load();
    }

    public List<ReminderModel> getData(){
        return data;
    }

    public int getCount(){
        return data != null ? data.size() : 0;
    }

    public boolean hasActive(){
        boolean res = false;
        for (ReminderModel item : data){
            if (item.getStatus() == 0) {
                res = true;
                break;
            }
        }
        return res;
    }

    public boolean isActive(int position){
        return data != null && data.get(position).getStatusDb() == Constants.ENABLE;
    }

    public int getPosition(ReminderModel item){
        int res = -1;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                ReminderModel item1 = data.get(i);
                if (item.getId() == item1.getId()) {
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    public int removeItem(ReminderModel item){
        int res = 0;
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++){
                ReminderModel item1 = data.get(i);
                if (item.getId() == item1.getId()) {
                    data.remove(i);
                    res = i;
                    break;
                }
            }
        }
        return res;
    }

    public void removeItem(int position){
        mLastRemovedData = data.remove(position);
        mLastRemovedPosition = position;
    }

    public void moveItem(int from, int to){
        if (to < 0 || to >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + to);
        }

        if (from == to) {
            return;
        }

        final ReminderModel item = data.remove(from);

        data.add(to, item);
        mLastRemovedPosition = -1;
    }

    public int undoLastRemoval() {
        if (mLastRemovedData != null) {
            int insertedPosition;
            if (mLastRemovedPosition >= 0 && mLastRemovedPosition < data.size()) {
                insertedPosition = mLastRemovedPosition;
            } else {
                insertedPosition = data.size();
            }

            data.add(insertedPosition, mLastRemovedData);

            mLastRemovedData = null;
            mLastRemovedPosition = -1;

            return insertedPosition;
        } else {
            return -1;
        }
    }

    public ReminderModel getItem(int index) {
        if (index < 0 || index >= getCount()) {
            return null;
        }

        return data.get(index);
    }

    public ArrayList<MarkerModel> getListData(){
        ArrayList<MarkerModel> list = new ArrayList<>();
        list.clear();
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor c = db.getReminders(Constants.ENABLE);
        if (c != null && c.moveToFirst()){
            do {
                String title = c.getString(c.getColumnIndex(DataBase.SUMMARY));
                long id = c.getLong(c.getColumnIndex(DataBase._ID));
                int icon = c.getInt(c.getColumnIndex(DataBase.MARKER));

                if (icon == -1) {
                    icon = new Coloring(mContext).getMarkerStyle();
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

    public void load(){
        data.clear();
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor c = db.getAllReminders();
        if (c != null && c.moveToFirst()){
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

                data.add(new ReminderModel(title, type, uuID, statusDb, startTime, id,
                        new double[]{lat, lon}, number, statusList, radius, melody));
            } while (c.moveToNext());
        }
    }

    public static ReminderModel getItem(Context mContext, long id){
        ReminderModel item = null;
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()){
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
        return item;
    }
}
