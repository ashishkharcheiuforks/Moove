package com.backdoor.moove.core.services;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.backdoor.moove.R;
import com.backdoor.moove.ReminderDialog;
import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.helper.DataBase;
import com.backdoor.moove.core.helper.Module;
import com.backdoor.moove.core.helper.SharedPrefs;
import com.backdoor.moove.core.utils.TimeUtil;
import com.backdoor.moove.core.utils.ViewUtils;
import com.backdoor.moove.core.widgets.LeftDistanceWidget;
import com.backdoor.moove.core.widgets.LeftDistanceWidgetConfigureActivity;

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
        double mLat = intent.getDoubleExtra("lat", 0);
        double mLon = intent.getDoubleExtra("lon", 0);
        Location locationA = new Location("point A");
        locationA.setLatitude(mLat);
        locationA.setLongitude(mLon);
        DataBase db = new DataBase(getApplicationContext());
        SharedPrefs prefs = new SharedPrefs(getApplicationContext());
        boolean isEnabled = prefs.loadBoolean(Prefs.TRACKING_NOTIFICATION);
        int stockRadius = prefs.loadInt(Prefs.LOCATION_RADIUS);
        boolean isWear = prefs.loadBoolean(Prefs.WEAR_NOTIFICATION);
        db.open();
        Cursor c = db.getReminders(Constants.ENABLE);
        if (c != null && c.moveToFirst()) {
            do{
                double lat = c.getDouble(c.getColumnIndex(DataBase.LATITUDE));
                double lon = c.getDouble(c.getColumnIndex(DataBase.LONGITUDE));
                long id = c.getLong(c.getColumnIndex(DataBase._ID));
                long startTime = c.getLong(c.getColumnIndex(DataBase.START_TIME));
                String task = c.getString(c.getColumnIndex(DataBase.SUMMARY));
                String type = c.getString(c.getColumnIndex(DataBase.TYPE));
                int status = c.getInt(c.getColumnIndex(DataBase.STATUS));
                int statusNot = c.getInt(c.getColumnIndex(DataBase.STATUS_NOTIFICATION));
                int statusRem = c.getInt(c.getColumnIndex(DataBase.STATUS_REMINDER));
                int radius = c.getInt(c.getColumnIndex(DataBase.RADIUS));
                int widgetId = c.getInt(c.getColumnIndex(DataBase.WIDGET_ID));

                if (radius == -1) {
                    radius = stockRadius;
                }
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
                                    showNotification(id, roundedDistance, statusNot, task, isWear);
                                }
                                updateWidget(widgetId, roundedDistance);
                            }
                        }
                    } else {
                        if (roundedDistance <= radius) {
                            if (statusRem != Constants.SHOWN) {
                                showReminder(id, task);
                            }
                        } else {
                            if (isEnabled) {
                                showNotification(id, roundedDistance, statusNot, task, isWear);
                            }
                            updateWidget(widgetId, roundedDistance);
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
                                        showNotification(id, roundedDistance, statusNot, task, isWear);
                                    }
                                    updateWidget(widgetId, roundedDistance);
                                }
                            }
                        } else {
                            if (roundedDistance <= radius) {
                                if (statusRem != Constants.SHOWN) {
                                    showReminder(id, task);
                                }
                            } else {
                                if (isEnabled) {
                                    showNotification(id, roundedDistance, statusNot, task, isWear);
                                }
                                updateWidget(widgetId, roundedDistance);
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

    private void updateWidget(int id, int distance) {
        Context context = getApplicationContext();
        LeftDistanceWidgetConfigureActivity.saveDistancePref(context, id, distance);

        Intent intent = new Intent(context, LeftDistanceWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{id});
        context.sendBroadcast(intent);
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
    }

    private void showNotification(long id, int roundedDistance, int shown, String task, boolean isWear){
        Integer i = (int) (long) id;
        Context context = getApplicationContext();
        String content = String.valueOf(roundedDistance);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentText(content);
        builder.setContentTitle(task);
        builder.setSmallIcon(R.drawable.ic_navigation_white_24dp);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Module.isLollipop()) {
            builder.setColor(ViewUtils.getColor(context, R.color.themePrimaryDark));
        }

        if (isWear) {
            if (Module.isJellyBean()) {
                builder.setOnlyAlertOnce(true);
                builder.setGroup("LOCATION");
                builder.setGroupSummary(true);
            }
        }

        if (shown != Constants.SHOWN) {
            DataBase db = new DataBase(context);
            db.open().setStatusNotification(id, Constants.SHOWN);
            db.close();
        }
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(context);
        mNotifyMgr.notify(i, builder.build());

        if (isWear){
            if (Module.isJellyBean()) {
                final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(context);
                wearableNotificationBuilder.setSmallIcon(R.drawable.ic_navigation_white_24dp);
                wearableNotificationBuilder.setContentTitle(task);
                wearableNotificationBuilder.setContentText(content);
                wearableNotificationBuilder.setOngoing(false);
                if (Module.isLollipop()) {
                    wearableNotificationBuilder.setColor(ViewUtils.getColor(context, R.color.themePrimaryDark));
                }
                wearableNotificationBuilder.setOnlyAlertOnce(true);
                wearableNotificationBuilder.setGroup("LOCATION");
                wearableNotificationBuilder.setGroupSummary(false);
                mNotifyMgr.notify(i, wearableNotificationBuilder.build());
            }
        }
    }

}