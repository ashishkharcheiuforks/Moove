package com.backdoor.moove.core.async;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;

import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.helper.DataBase;
import com.backdoor.moove.core.services.CheckPosition;
import com.backdoor.moove.core.services.GeolocationService;
import com.backdoor.moove.core.utils.TimeUtil;

public class DisableAsync extends AsyncTask<Void, Void, Void> {

    private Context mContext;

    public DisableAsync(Context context){
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor c = db.getReminders(Constants.ENABLE);
        if (c != null && c.moveToFirst()){
            boolean res = false;
            do {
                String tp = c.getString(c.getColumnIndex(DataBase.TYPE));
                if (tp.startsWith(Constants.TYPE_LOCATION) || tp.startsWith(Constants.TYPE_LOCATION_OUT)) {
                    long startTime = c.getInt(c.getColumnIndex(DataBase.START_TIME));
                    int isShown = c.getInt(c.getColumnIndex(DataBase.STATUS));
                    int isDone = c.getInt(c.getColumnIndex(DataBase.STATUS_DB));
                    if (startTime == 0) {
                        if (isDone != 1){
                            if (isShown != Constants.SHOWN) {
                                res = true;
                            }
                        }
                    } else {
                        if (TimeUtil.isCurrent(startTime)) {
                            if (isDone != 1){
                                if (isShown != Constants.SHOWN) {
                                    res = true;
                                }
                            }
                        }
                    }
                }
            } while (c.moveToNext());
            if (!res) {
                mContext.stopService(new Intent(mContext, GeolocationService.class));
                mContext.stopService(new Intent(mContext, CheckPosition.class));
            } else {
                mContext.startService(new Intent(mContext, GeolocationService.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        } else {
            mContext.stopService(new Intent(mContext, GeolocationService.class));
            mContext.stopService(new Intent(mContext, CheckPosition.class));
        }
        if (c != null) {
            c.close();
        }
        db.close();
        return null;
    }
}
