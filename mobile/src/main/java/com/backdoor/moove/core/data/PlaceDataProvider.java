package com.backdoor.moove.core.data;

import android.content.Context;
import android.database.Cursor;

import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.helper.DataBase;
import com.backdoor.moove.core.helper.SharedPrefs;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class PlaceDataProvider {
    private List<MarkerModel> data;
    private Context mContext;

    public PlaceDataProvider(Context mContext, boolean list) {
        data = new ArrayList<>();
        this.mContext = mContext;
        if (list) {
            loadPlaces();
        } else {
            loadReminders();
        }
    }

    public List<MarkerModel> getData() {
        return data;
    }

    public int getCount() {
        return data != null ? data.size() : 0;
    }

    public MarkerModel getItem(int index) {
        if (index < 0 || index >= getCount()) {
            return null;
        }

        return data.get(index);
    }

    private void loadReminders() {
        data.clear();
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor c = db.getReminders(Constants.ENABLE);
        if (c != null && c.moveToNext()) {
            do {
                String text = c.getString(c.getColumnIndex(DataBase.SUMMARY));
                long id = c.getLong(c.getColumnIndex(DataBase._ID));
                double latitude = c.getDouble(c.getColumnIndex(DataBase.LATITUDE));
                double longitude = c.getDouble(c.getColumnIndex(DataBase.LONGITUDE));
                int style = c.getInt(c.getColumnIndex(DataBase.MARKER));
                int radius = c.getInt(c.getColumnIndex(DataBase.RADIUS));
                if (radius == -1) {
                    radius = new SharedPrefs(mContext).loadInt(Prefs.LOCATION_RADIUS);
                }
                data.add(new MarkerModel(text, new LatLng(latitude, longitude), style, id, radius));
            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
        db.close();
    }

    public void loadPlaces() {
        data.clear();
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor c = db.queryPlaces();
        if (c != null && c.moveToNext()) {
            do {
                String text = c.getString(c.getColumnIndex(DataBase.NAME));
                long id = c.getLong(c.getColumnIndex(DataBase._ID));
                data.add(new MarkerModel(text, id));
            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
        db.close();
    }
}
