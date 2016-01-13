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
            int i = 0;
            do {
                long startTime = c.getInt(c.getColumnIndex(DataBase.START_TIME));
                int isShown = c.getInt(c.getColumnIndex(DataBase.STATUS_REMINDER));
                if (startTime == -1) {
                    if (isShown != Constants.SHOWN) {
                        i++;
                    }
                } else {
                    if (TimeUtil.isCurrent(startTime)) {
                        if (isShown != Constants.SHOWN) {
                            i++;
                        }
                    }
                }
            } while (c.moveToNext());
            if (i == 0) {
                mContext.stopService(new Intent(mContext, GeolocationService.class));
            }
        } else {
            mContext.stopService(new Intent(mContext, GeolocationService.class));
        }
        if (c != null) {
            c.close();
        }
        db.close();
        return null;
    }
}
