package com.backdoor.moove.core.helper;

import android.content.Context;
import android.database.Cursor;

public class Type {

    private Context mContext;
    private String type;

    public Type(Context context){
        this.mContext = context;
        this.type = "";
    }

    /**
     * Get reminder object.
     * @param id reminder identifier.
     * @return reminder object
     */
    public Reminder getItem(long id){
        DataBase db = new DataBase(mContext);
        db.open();
        Cursor c = db.getReminder(id);
        if (c != null && c.moveToFirst()){
            String summary = c.getString(c.getColumnIndex(DataBase.SUMMARY));
            String number = c.getString(c.getColumnIndex(DataBase.NUMBER));
            long due = c.getLong(c.getColumnIndex(DataBase.START_TIME));
            String type = c.getString(c.getColumnIndex(DataBase.TYPE));
            int radius = c.getInt(c.getColumnIndex(DataBase.RADIUS));
            int ledColor = c.getInt(c.getColumnIndex(DataBase.LED_COLOR));
            int marker = c.getInt(c.getColumnIndex(DataBase.MARKER));
            int volume = c.getInt(c.getColumnIndex(DataBase.VOLUME));
            String melody = c.getString(c.getColumnIndex(DataBase.MELODY));
            String uuId = c.getString(c.getColumnIndex(DataBase.UUID));
            double latitude = c.getDouble(c.getColumnIndex(DataBase.LATITUDE));
            double longitude = c.getDouble(c.getColumnIndex(DataBase.LONGITUDE));

            c.close();
            db.close();

            return new Reminder(id, summary, type, melody, uuId,
                    new double[]{latitude, longitude}, number,
                    radius, due, ledColor, marker, volume);
        } else return null;
    }

    /**
     * Set reminder type.
     * @param type reminder type.
     */
    public void setType(String type){
        this.type = type;
    }

    /**
     * Get reminder type.
     * @return reminder type
     */
    public String getType(){
        return type;
    }

    /**
     * Save new reminder to database.
     * @param item reminder object.
     * @return reminder identifier
     */
    public long save(Reminder item){
        DataBase db = new DataBase(mContext);
        db.open();
        long id = db.insertReminder(item.getTitle(), item.getType(),
                item.getNumber(), item.getStartTime(), item.getPlace()[0],
                item.getPlace()[1], item.getUuId(), item.getMelody(),
                item.getRadius(), item.getColor(), item.getMarker(), item.getVolume());
        db.close();
        return id;
    }

    /**
     * Update reminder in database.
     * @param id reminder identifier.
     * @param item reminder object.
     */
    public void save(long id, Reminder item){
        DataBase db = new DataBase(mContext);
        db.open();
        db.updateReminder(id, item.getTitle(), item.getType(), item.getNumber(),
                item.getStartTime(), item.getPlace()[0], item.getPlace()[1],
                item.getMelody(), item.getRadius(), item.getColor(), item.getMarker(),
                item.getVolume());
        db.close();
    }
}
