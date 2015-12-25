package com.backdoor.moove.core.helper;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.backdoor.moove.R;
import com.backdoor.moove.ReminderManager;
import com.backdoor.moove.core.async.DisableAsync;
import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.interfaces.ActionCallbacks;
import com.backdoor.moove.core.services.CheckPosition;
import com.backdoor.moove.core.services.GeolocationService;
import com.backdoor.moove.core.services.PositionDelayReceiver;
import com.backdoor.moove.core.utils.LocationUtil;

/**
 * Helper class for interaction with reminders.
 */
public class Reminder {

    private String title, type, uuId, number, melody, group;
    private int radius, color;
    private long id, startTime;
    private double[] place;

    public Reminder(long id, String title, String type, String melody, String uuId,
                    double[] place, String number, int radius, long startTime,
                    int color){
        this.id = id;
        this.title = title;
        this.type = type;
        this.melody = melody;
        this.uuId = uuId;
        this.place = place;
        this.number = number;
        this.radius = radius;
        this.color = color;
        this.startTime = startTime;
    }

    /**
     * Toggle reminder status.
     * @param id reminder identifier.
     * @param context application context.
     * @return boolean
     */
    public static boolean toggle(long id, Context context, ActionCallbacks callbacks){
        DataBase db = new DataBase(context);
        db.open();
        Cursor c = db.getReminder(id);
        String type = null;
        long startTime = 0;
        int status = Constants.ENABLE;
        if (c != null && c.moveToFirst()) {
            startTime = c.getLong(c.getColumnIndex(DataBase.START_TIME));
            status = c.getInt(c.getColumnIndex(DataBase.STATUS_DB));
        }
        if (c != null) c.close();
        boolean res;
        if (status == Constants.ENABLE){
            disableReminder(id, context);
            res = true;
        } else {
            if (!LocationUtil.checkLocationEnable(context)){
                LocationUtil.showLocationAlert(context);
                res = false;
            } else {
                db.setStatus(id, Constants.ENABLE);
                if (startTime == 0) {
                    context.startService(new Intent(context, GeolocationService.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    context.startService(new Intent(context, CheckPosition.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } else {
                    new PositionDelayReceiver().setAlarm(context, id);
                }
                res = true;
            }
        }
        db.close();
        return res;
    }

    /**
     * Disable reminder.
     * @param id reminder identifier.
     * @param context application context.
     */
    public static void disableReminder(long id, Context context){
        DataBase db = new DataBase(context);
        if (!db.isOpen()) {
            db.open();
        }
        db.setStatus(id, Constants.DISABLE);
        db.close();
        disable(context, id);
    }

    /**
     * Disable all available reminder notifications.
     * @param context application context.
     * @param id reminder identifier.
     */
    private static void disable(Context context, long id) {
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Integer i = (int) (long) id;
        mNotifyMgr.cancel(i);
        new PositionDelayReceiver().cancelAlarm(context, i);
        new DisableAsync(context).execute();
    }

    /**
     * Move reminder to trash.
     * @param id reminder identifier.
     * @param context application context.
     */
    public static void moveToTrash(long id, Context context, ActionCallbacks callbacks){
        DataBase db = new DataBase(context);
        if (!db.isOpen()) {
            db.open();
        }
        db.setStatus(id, Constants.ARCHIVE);
        db.close();
        disable(context, id);
        if (callbacks != null) {
            callbacks.showSnackbar(R.string.deleted);
        } else {
            Messages.toast(context, R.string.deleted);
        }
    }

    /**
     * Edit reminder.
     * @param id reminder identifier.
     * @param context application context.
     */
    public static void edit(long id, Context context){
        disable(context, id);
        Intent intentId = new Intent(context, ReminderManager.class);
        intentId.putExtra(Constants.EDIT_ID, id);
        context.startActivity(intentId);
    }

    /**
     * Delete reminder from application.
     * @param id reminder identifier.
     * @param context application context.
     */
    public static void delete(long id, Context context) {
        DataBase db = new DataBase(context);
        if (!db.isOpen()) {
            db.open();
        }
        db.deleteReminder(id);
        db.close();
        disable(context, id);
    }

    public String getMelody(){
        return melody;
    }

    public void setMelody(String melody){
        this.melody = melody;
    }

    public int getRadius(){
        return radius;
    }

    public void setRadius(int radius){
        this.radius = radius;
    }

    public int getColor(){
        return color;
    }

    public void setColor(int color){
        this.color = color;
    }

    public double[] getPlace(){
        return place;
    }

    public void  setPlace(double[] place){
        this.place = place;
    }

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getUuId(){
        return uuId;
    }

    public void setUuId(String uuId){
        this.uuId = uuId;
    }

    public String getNumber(){
        return number;
    }

    public void setNumber(String number){
        this.number = number;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
