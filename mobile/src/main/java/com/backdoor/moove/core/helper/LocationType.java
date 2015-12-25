package com.backdoor.moove.core.helper;

import android.content.Context;
import android.content.Intent;

import com.backdoor.moove.core.services.CheckPosition;
import com.backdoor.moove.core.services.GeolocationService;
import com.backdoor.moove.core.services.PositionDelayReceiver;

public class LocationType extends Type {

    private Context mContext;

    public LocationType(Context context, String type) {
        super(context);
        this.mContext = context;
        setType(type);
    }

    @Override
    public long save(Reminder item) {
        long id = super.save(item);
        startTracking(id, item);
        return id;
    }

    @Override
    public void save(long id, Reminder item) {
        super.save(id, item);
        startTracking(id, item);
    }

    private void startTracking(long id, Reminder item) {
        if (item.getStartTime() > 0) {
            new PositionDelayReceiver().setAlarm(mContext, id);
        } else {
            mContext.startService(new Intent(mContext, GeolocationService.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            mContext.startService(new Intent(mContext, CheckPosition.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }
}
