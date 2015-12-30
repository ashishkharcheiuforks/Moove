package com.backdoor.moove.core.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.backdoor.moove.R;
import com.backdoor.moove.ReminderDialog;
import com.backdoor.moove.core.async.DisableAsync;
import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.helper.DataBase;
import com.backdoor.moove.core.helper.SharedPrefs;
import com.backdoor.moove.core.utils.TimeUtil;

public class CheckPosition extends IntentService {

    public CheckPosition() {
        super("CheckPosition");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    protected void onHandleIntent(final Intent intent) {
        double currentLat = intent.getDoubleExtra("lat", 0);
        double currentLong = intent.getDoubleExtra("lon", 0);
        Location locationA = new Location("point A");
        locationA.setLatitude(currentLat);
        locationA.setLongitude(currentLong);
        DataBase db = new DataBase(getApplicationContext());
        SharedPrefs sPrefs = new SharedPrefs(getApplicationContext());
        boolean isEnabled = sPrefs.loadBoolean(Prefs.TRACKING_NOTIFICATION);
        db.open();
        Cursor c = db.getAllReminders();
        if (c != null && c.moveToFirst()) {
            do{
                double lat = c.getDouble(c.getColumnIndex(DataBase.LATITUDE));
                double lon = c.getDouble(c.getColumnIndex(DataBase.LONGITUDE));
                long id = c.getLong(c.getColumnIndex(DataBase._ID));
                long startTime = c.getLong(c.getColumnIndex(DataBase.START_TIME));
                String task = c.getString(c.getColumnIndex(DataBase.SUMMARY));
                String type = c.getString(c.getColumnIndex(DataBase.TYPE));
                int status = c.getInt(c.getColumnIndex(DataBase.STATUS));
                int statusDb = c.getInt(c.getColumnIndex(DataBase.STATUS_DB));
                int statusNot = c.getInt(c.getColumnIndex(DataBase.STATUS_NOTIFICATION));
                int statusRem = c.getInt(c.getColumnIndex(DataBase.STATUS_REMINDER));
                int radius = c.getInt(c.getColumnIndex(DataBase.RADIUS));
                int stockRadius = sPrefs.loadInt(Prefs.LOCATION_RADIUS);
                if (radius == -1) {
                    radius = stockRadius;
                }
                if (statusDb == Constants.ENABLE) {
                    if (startTime <= 0) {
                        Location locationB = new Location("point B");
                        locationB.setLatitude(lat);
                        locationB.setLongitude(lon);
                        float distance = locationA.distanceTo(locationB);
                        int roundedDistance = Math.round(distance);
                        if (type.startsWith(Constants.TYPE_LOCATION_OUT)){
                            if (status == Constants.NOT_LOCKED){
                                if (roundedDistance < radius) {
                                    db.setLocationStatus(id, Constants.LOCKED);
                                }
                            } else {
                                if (roundedDistance > radius) {
                                    if (statusRem != Constants.SHOWN) {
                                        showReminder(id, task);
                                    }
                                } else {
                                    if (isEnabled) {
                                        showNotification(id, roundedDistance, statusNot, task);
                                    }
                                }
                            }
                        } else {
                            if (roundedDistance <= radius) {
                                if (statusRem != Constants.SHOWN) {
                                    showReminder(id, task);
                                }
                            } else {
                                if (isEnabled) {
                                    showNotification(id, roundedDistance, statusNot, task);
                                }
                            }
                        }
                    } else {
                        if (TimeUtil.isCurrent(startTime)) {
                            Location locationB = new Location("point B");
                            locationB.setLatitude(lat);
                            locationB.setLongitude(lon);
                            float distance = locationA.distanceTo(locationB);
                            int roundedDistance = Math.round(distance);
                            if (type.startsWith(Constants.TYPE_LOCATION_OUT)){
                                if (status == Constants.NOT_LOCKED){
                                    if (roundedDistance <= radius) {
                                        db.setLocationStatus(id, Constants.LOCKED);
                                    }
                                } else {
                                    if (roundedDistance > radius) {
                                        if (statusRem != Constants.SHOWN) {
                                            showReminder(id, task);
                                        }
                                    } else {
                                        if (isEnabled) {
                                            showNotification(id, roundedDistance, statusNot, task);
                                        }
                                    }
                                }
                            } else {
                                if (roundedDistance <= radius) {
                                    if (statusRem != Constants.SHOWN) {
                                        showReminder(id, task);
                                    }
                                } else {
                                    if (isEnabled) {
                                        showNotification(id, roundedDistance, statusNot, task);
                                    }
                                }
                            }
                        }
                    }
                }
            } while (c.moveToNext());
        } else {
            getApplication().stopService(new Intent(getApplicationContext(), GeolocationService.class));
            stopSelf();
        }

        if (c != null) {
            c.close();
        }

        db.close();
    }

    private void showReminder(long id, String task){
        DataBase db = new DataBase(getApplicationContext());
        db.open().setReminderStatus(id, Constants.SHOWN);
        db.close();
        Intent resultIntent = new Intent(getApplicationContext(), ReminderDialog.class);
        resultIntent.putExtra("taskDialog", task);
        resultIntent.putExtra(Constants.ITEM_ID_INTENT, id);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplication().startActivity(resultIntent);
        stopIt();
    }

    private void showNotification(long id, int roundedDistance, int shown, String task){
        Integer i = (int) (long) id;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentText(String.valueOf(roundedDistance));
        if (shown != Constants.NOT_SHOWN) {
            builder.setContentTitle(task);
            builder.setContentText(String.valueOf(roundedDistance));
            builder.setSmallIcon(R.drawable.ic_navigation_white_24dp);
        }
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotifyMgr.notify(i, builder.build());
    }

    private void stopIt(){
        new DisableAsync(getApplicationContext()).execute();
    }
}