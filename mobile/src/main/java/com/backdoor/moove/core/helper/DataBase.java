package com.backdoor.moove.core.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.backdoor.moove.core.consts.Constants;

public class DataBase {

    public static final String _ID = "_id";
    public static final String SUMMARY = "summary";
    public static final String TYPE = "type";
    public static final String GROUP = "_group";
    public static final String NUMBER = "number";
    public static final String NOTE = "note";
    public static final String VOLUME = "volume";
    public static final String START_TIME = "start_time";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String UUID = "uuid";
    public static final String STATUS = "status";
    public static final String STATUS_DB = "db_status";
    public static final String STATUS_REMINDER = "list_status";
    public static final String STATUS_NOTIFICATION = "n_status";
    public static final String MELODY = "melody";
    public static final String RADIUS = "radius";
    public static final String LED_COLOR = "led_color";
    public static final String MARKER = "marker";
    public static final String NAME = "place_name";

    private static final String DB_NAME = "moove_db";
    private static final int DB_VERSION = 1;
    private static final String CURRENT_TABLE_NAME = "moove_table";
    private static final String LOCATION_TABLE_NAME = "locations_table";

    private DBHelper dbHelper;
    private static Context mContext;
    private SQLiteDatabase db;

    private static final String CURRENT_TABLE_CREATE =
            "create table " + CURRENT_TABLE_NAME + "(" +
                    _ID + " integer primary key autoincrement, " +
                    SUMMARY + " VARCHAR(255), " +
                    TYPE + " VARCHAR(255), " +
                    GROUP + " VARCHAR(255), " +
                    STATUS + " INTEGER, " +
                    STATUS_DB + " INTEGER, " +
                    STATUS_REMINDER + " INTEGER, " +
                    STATUS_NOTIFICATION + " INTEGER, " +
                    RADIUS + " INTEGER, " +
                    NUMBER + " VARCHAR(255), " +
                    MELODY + " VARCHAR(255), " +
                    NOTE + " VARCHAR(255), " +
                    START_TIME + " INTEGER, " +
                    VOLUME + " INTEGER, " +
                    LED_COLOR + " INTEGER, " +
                    MARKER + " INTEGER, " +
                    LATITUDE + " REAL, " +
                    LONGITUDE + " REAL, " +
                    UUID + " VARCHAR(255)" +
                    ");";

    private static final String LOCATION_TABLE_CREATE =
            "create table " + LOCATION_TABLE_NAME + "(" +
                    _ID + " integer primary key autoincrement, " +
                    NAME + " VARCHAR(255), " +
                    LATITUDE + " REAL, " +
                    LONGITUDE + " REAL" +
                    ");";

    public class DBHelper extends SQLiteOpenHelper {


        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CURRENT_TABLE_CREATE);
            sqLiteDatabase.execSQL(LOCATION_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    public DataBase(Context c) {
        mContext = c;
    }

    public DataBase open() throws SQLiteException {
        dbHelper = new DBHelper(mContext);

        db = dbHelper.getWritableDatabase();

        System.gc();
        return this;
    }

    public boolean isOpen () {
        return db != null && db.isOpen();
    }

    public SQLiteDatabase getDatabase() {
        return db;
    }

    public void close() {
        if( dbHelper != null )
            dbHelper.close();
    }

    // Reminders database

    public long insertReminder(String summary, String type, String number, long startTime,
                               double latitude, double longitude, String uID, String melody,
                               int radius, int color, int volume) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(SUMMARY, summary);
        cv.put(TYPE, type);
        cv.put(NUMBER, number);
        cv.put(STATUS, Constants.NOT_LOCKED);
        cv.put(STATUS_DB, Constants.ENABLE);
        cv.put(STATUS_REMINDER, Constants.ACTIVE);
        cv.put(STATUS_NOTIFICATION, Constants.NOT_SHOWN);
        cv.put(MARKER, 1);
        cv.put(START_TIME, startTime);
        cv.put(LATITUDE, latitude);
        cv.put(LONGITUDE, longitude);
        cv.put(UUID, uID);
        cv.put(RADIUS, radius);
        cv.put(MELODY, melody);
        cv.put(LED_COLOR, color);
        cv.put(VOLUME, volume);
        return db.insert(CURRENT_TABLE_NAME, null, cv);
    }

    public long insertReminder(String summary, String type, String number, long startTime,
                               double latitude, double longitude, String uID, String melody,
                               int radius, int color, int marker, int volume) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(SUMMARY, summary);
        cv.put(TYPE, type);
        cv.put(NUMBER, number);
        cv.put(STATUS, Constants.NOT_LOCKED);
        cv.put(STATUS_DB, Constants.ENABLE);
        cv.put(STATUS_REMINDER, Constants.ACTIVE);
        cv.put(STATUS_NOTIFICATION, Constants.NOT_SHOWN);
        cv.put(MARKER, marker);
        cv.put(START_TIME, startTime);
        cv.put(LATITUDE, latitude);
        cv.put(LONGITUDE, longitude);
        cv.put(UUID, uID);
        cv.put(RADIUS, radius);
        cv.put(MELODY, melody);
        cv.put(LED_COLOR, color);
        cv.put(VOLUME, volume);
        return db.insert(CURRENT_TABLE_NAME, null, cv);
    }

    public boolean updateReminder(long rowId, String summary, String type, String number,
                                  long startTime, double latitude, double longitude,
                                  String melody, int radius, int color, int volume) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(SUMMARY, summary);
        args.put(TYPE, type);
        args.put(NUMBER, number);
        args.put(STATUS, Constants.NOT_LOCKED);
        args.put(STATUS_DB, Constants.ENABLE);
        args.put(STATUS_REMINDER, Constants.ACTIVE);
        args.put(STATUS_NOTIFICATION, Constants.NOT_SHOWN);
        args.put(START_TIME, startTime);
        args.put(LATITUDE, latitude);
        args.put(LONGITUDE, longitude);
        args.put(RADIUS, radius);
        args.put(MELODY, melody);
        args.put(LED_COLOR, color);
        args.put(VOLUME, volume);
        return db.update(CURRENT_TABLE_NAME, args, _ID + "=" + rowId, null) > 0;
    }

    public boolean updateReminder(long rowId, String summary, String type, String number,
                                  long startTime, double latitude, double longitude,
                                  String melody, int radius, int color, int marker, int volume) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(SUMMARY, summary);
        args.put(TYPE, type);
        args.put(NUMBER, number);
        args.put(STATUS, Constants.NOT_LOCKED);
        args.put(STATUS_DB, Constants.ENABLE);
        args.put(STATUS_REMINDER, Constants.ACTIVE);
        args.put(STATUS_NOTIFICATION, Constants.NOT_SHOWN);
        args.put(START_TIME, startTime);
        args.put(MARKER, marker);
        args.put(LATITUDE, latitude);
        args.put(LONGITUDE, longitude);
        args.put(RADIUS, radius);
        args.put(MELODY, melody);
        args.put(LED_COLOR, color);
        args.put(VOLUME, volume);
        return db.update(CURRENT_TABLE_NAME, args, _ID + "=" + rowId, null) > 0;
    }

    public boolean setStatus(long rowId, int status) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(STATUS_DB, status);
        return db.update(CURRENT_TABLE_NAME, args, _ID + "=" + rowId, null) > 0;
    }

    public boolean setStatusNotification(long rowId, int status) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(STATUS_NOTIFICATION, status);
        return db.update(CURRENT_TABLE_NAME, args, _ID + "=" + rowId, null) > 0;
    }

    public boolean setReminderStatus(long rowId, int status) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(STATUS_REMINDER, status);
        return db.update(CURRENT_TABLE_NAME, args, _ID + "=" + rowId, null) > 0;
    }

    public boolean setLocationStatus(long rowId, int status) {
        openGuard();
        ContentValues args = new ContentValues();
        args.put(STATUS, status);
        return db.update(CURRENT_TABLE_NAME, args, _ID + "=" + rowId, null) > 0;
    }

    public Cursor getAllReminders() throws SQLException {
        openGuard();
        return db.query(CURRENT_TABLE_NAME, null, null, null, null, null, STATUS_DB + " ASC");
    }

    public Cursor getReminders(int status) throws SQLException {
        openGuard();
        return db.query(CURRENT_TABLE_NAME, null, STATUS_REMINDER + "='" + status + "'",
                null, null, null, null);
    }

    public Cursor getReminder(long rowId) throws SQLException {
        openGuard();
        return db.query(CURRENT_TABLE_NAME, null, _ID  + "=" + rowId, null, null, null,
                null, null);
    }

    public Cursor getReminder(String uuID) throws SQLException {
        openGuard();
        return db.query(CURRENT_TABLE_NAME, null, UUID  + "='" + uuID + "'", null, null, null,
                null, null);
    }

    public Cursor getMarkers() throws SQLException {
        openGuard();
        return db.query(CURRENT_TABLE_NAME, null, TYPE  + "='" + Constants.TYPE_LOCATION +
                "'" + " OR "+ TYPE + "='" + Constants.TYPE_LOCATION_OUT_MESSAGE + "'" +
                " AND "+ STATUS_DB + "='" + Constants.ENABLE + "'", null, null, null, null, null);
    }

    public boolean deleteReminder(long rowId) {
        openGuard();
        return db.delete(CURRENT_TABLE_NAME, _ID + "=" + rowId, null) > 0;
    }

    //Frequently used places database

    public boolean deletePlace(long rowId) {
        openGuard();
        return db.delete(LOCATION_TABLE_NAME, _ID + "=" + rowId, null) > 0;
    }

    public Cursor getPlace(String name) throws SQLException {
        openGuard();
        return db.query(LOCATION_TABLE_NAME, null, NAME  + "='" + name + "'",
                null, null, null, null, null);
    }

    public Cursor getPlace(long id) throws SQLException {
        openGuard();
        return db.query(LOCATION_TABLE_NAME, null, _ID  + "=" + id, null, null,
                null, null, null);
    }

    public Cursor queryPlaces() throws SQLException {
        openGuard();
        return db.query(LOCATION_TABLE_NAME, null, null, null, null, null, null);
    }

    public long insertPlace (String name, double latitude, double longitude) {
        openGuard();
        ContentValues cv = new ContentValues();
        cv.put(NAME, name);
        cv.put(LATITUDE, latitude);
        cv.put(LONGITUDE, longitude);
        return db.insert(LOCATION_TABLE_NAME, null, cv);
    }

    public boolean updatePlace(long rowId, String name, double latitude, double longitude){
        openGuard();
        ContentValues args = new ContentValues();
        args.put(NAME, name);
        args.put(LATITUDE, latitude);
        args.put(LONGITUDE, longitude);
        return db.update(LOCATION_TABLE_NAME, args,  _ID + "=" + rowId, null) > 0;
    }

    public void openGuard() throws SQLiteException {
        if(isOpen()) return;
        open();
        if(isOpen()) return;
        //Log.d(LOG_TAG, "open guard failed");
        throw new SQLiteException("Could not open database");
    }
}