package com.backdoor.moove.core.helper

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper

import com.backdoor.moove.core.consts.Constants

class DataBase(c: Context) {

    private var dbHelper: DBHelper? = null
    var database: SQLiteDatabase? = null
        private set

    val isOpen: Boolean
        get() = database != null && database!!.isOpen

    val allReminders: Cursor
        @Throws(SQLException::class)
        get() {
            openGuard()
            return database!!.query(CURRENT_TABLE_NAME, null, null, null, null, null, "$STATUS_DB ASC")
        }

    inner class DBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

        override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
            sqLiteDatabase.execSQL(CURRENT_TABLE_CREATE)
            sqLiteDatabase.execSQL(LOCATION_TABLE_CREATE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        }
    }

    init {
        mContext = c
    }

    @Throws(SQLiteException::class)
    fun open(): DataBase {
        dbHelper = DBHelper(mContext)

        database = dbHelper!!.writableDatabase

        System.gc()
        return this
    }

    fun close() {
        if (dbHelper != null)
            dbHelper!!.close()
    }

    // Reminders database

    fun insertReminder(summary: String, type: String, number: String, startTime: Long,
                       latitude: Double, longitude: Double, uID: String, melody: String,
                       radius: Int, color: Int, marker: Int, volume: Int): Long {
        openGuard()
        val cv = ContentValues()
        cv.put(SUMMARY, summary)
        cv.put(TYPE, type)
        cv.put(NUMBER, number)
        cv.put(STATUS, Constants.NOT_LOCKED)
        cv.put(STATUS_DB, Constants.ENABLE)
        cv.put(STATUS_REMINDER, Constants.NOT_SHOWN)
        cv.put(STATUS_NOTIFICATION, Constants.NOT_SHOWN)
        cv.put(MARKER, marker)
        cv.put(START_TIME, startTime)
        cv.put(LATITUDE, latitude)
        cv.put(LONGITUDE, longitude)
        cv.put(UUID, uID)
        cv.put(RADIUS, radius)
        cv.put(MELODY, melody)
        cv.put(LED_COLOR, color)
        cv.put(VOLUME, volume)
        cv.put(WIDGET_ID, "")
        return database!!.insert(CURRENT_TABLE_NAME, null, cv)
    }

    fun updateReminder(rowId: Long, summary: String, type: String, number: String,
                       startTime: Long, latitude: Double, longitude: Double,
                       melody: String, radius: Int, color: Int, marker: Int, volume: Int): Boolean {
        openGuard()
        val args = ContentValues()
        args.put(SUMMARY, summary)
        args.put(TYPE, type)
        args.put(NUMBER, number)
        args.put(STATUS, Constants.NOT_LOCKED)
        args.put(STATUS_DB, Constants.ENABLE)
        args.put(STATUS_REMINDER, Constants.NOT_SHOWN)
        args.put(STATUS_NOTIFICATION, Constants.NOT_SHOWN)
        args.put(START_TIME, startTime)
        args.put(MARKER, marker)
        args.put(LATITUDE, latitude)
        args.put(LONGITUDE, longitude)
        args.put(RADIUS, radius)
        args.put(MELODY, melody)
        args.put(LED_COLOR, color)
        args.put(VOLUME, volume)
        return database!!.update(CURRENT_TABLE_NAME, args, "$_ID=$rowId", null) > 0
    }

    fun setStatus(rowId: Long, status: Int): Boolean {
        openGuard()
        val args = ContentValues()
        args.put(STATUS_DB, status)
        return database!!.update(CURRENT_TABLE_NAME, args, "$_ID=$rowId", null) > 0
    }

    fun setWidgetId(rowId: Long, prefs: String): Boolean {
        openGuard()
        val args = ContentValues()
        args.put(WIDGET_ID, prefs)
        return database!!.update(CURRENT_TABLE_NAME, args, "$_ID=$rowId", null) > 0
    }

    fun removeWidget(rowId: Long): Boolean {
        openGuard()
        val args = ContentValues()
        args.put(WIDGET_ID, "")
        return database!!.update(CURRENT_TABLE_NAME, args, "$_ID=$rowId", null) > 0
    }

    fun setStatusNotification(rowId: Long, status: Int): Boolean {
        openGuard()
        val args = ContentValues()
        args.put(STATUS_NOTIFICATION, status)
        return database!!.update(CURRENT_TABLE_NAME, args, "$_ID=$rowId", null) > 0
    }

    fun setReminderStatus(rowId: Long, status: Int): Boolean {
        openGuard()
        val args = ContentValues()
        args.put(STATUS_REMINDER, status)
        return database!!.update(CURRENT_TABLE_NAME, args, "$_ID=$rowId", null) > 0
    }

    fun setLocationStatus(rowId: Long, status: Int): Boolean {
        openGuard()
        val args = ContentValues()
        args.put(STATUS, status)
        return database!!.update(CURRENT_TABLE_NAME, args, "$_ID=$rowId", null) > 0
    }

    @Throws(SQLException::class)
    fun getReminders(status: Int): Cursor {
        openGuard()
        return database!!.query(CURRENT_TABLE_NAME, null, "$STATUS_DB='$status'", null, null, null, null)
    }

    @Throws(SQLException::class)
    fun getRemindersWithWidget(prefs: String): Cursor {
        openGuard()
        return database!!.query(CURRENT_TABLE_NAME, null, "$WIDGET_ID='$prefs'", null, null, null, null)
    }

    @Throws(SQLException::class)
    fun getReminder(rowId: Long): Cursor {
        openGuard()
        return database!!.query(CURRENT_TABLE_NAME, null, "$_ID=$rowId", null, null, null, null, null)
    }

    fun deleteReminder(rowId: Long): Boolean {
        openGuard()
        return database!!.delete(CURRENT_TABLE_NAME, "$_ID=$rowId", null) > 0
    }

    //Frequently used places database

    fun deletePlace(rowId: Long): Boolean {
        openGuard()
        return database!!.delete(LOCATION_TABLE_NAME, "$_ID=$rowId", null) > 0
    }

    @Throws(SQLException::class)
    fun getPlace(name: String): Cursor {
        openGuard()
        return database!!.query(LOCATION_TABLE_NAME, null, "$NAME='$name'", null, null, null, null, null)
    }

    @Throws(SQLException::class)
    fun getPlace(latitude: Double, longitude: Double): Cursor {
        openGuard()
        return database!!.query(LOCATION_TABLE_NAME, null, LATITUDE + "=" + latitude +
                " AND " + LONGITUDE + "=" + longitude, null, null, null, null, null)
    }

    @Throws(SQLException::class)
    fun getPlace(id: Long): Cursor {
        openGuard()
        return database!!.query(LOCATION_TABLE_NAME, null, "$_ID=$id", null, null, null, null, null)
    }

    @Throws(SQLException::class)
    fun queryPlaces(): Cursor {
        openGuard()
        return database!!.query(LOCATION_TABLE_NAME, null, null, null, null, null, null)
    }

    fun insertPlace(name: String, latitude: Double, longitude: Double): Long {
        openGuard()
        val cv = ContentValues()
        cv.put(NAME, name)
        cv.put(LATITUDE, latitude)
        cv.put(LONGITUDE, longitude)
        return database!!.insert(LOCATION_TABLE_NAME, null, cv)
    }

    fun updatePlace(rowId: Long, name: String, latitude: Double, longitude: Double): Boolean {
        openGuard()
        val args = ContentValues()
        args.put(NAME, name)
        args.put(LATITUDE, latitude)
        args.put(LONGITUDE, longitude)
        return database!!.update(LOCATION_TABLE_NAME, args, "$_ID=$rowId", null) > 0
    }

    @Throws(SQLiteException::class)
    fun openGuard() {
        if (isOpen) return
        open()
        if (isOpen) return
        //Log.d(LOG_TAG, "open guard failed");
        throw SQLiteException("Could not open database")
    }

    companion object {

        val _ID = "_id"
        val SUMMARY = "summary"
        val TYPE = "type"
        val GROUP = "_group"
        val NUMBER = "number"
        val NOTE = "note"
        val VOLUME = "volume"
        val START_TIME = "start_time"
        val LATITUDE = "latitude"
        val LONGITUDE = "longitude"
        val UUID = "uuid"
        val STATUS = "status"
        val STATUS_DB = "db_status"
        val STATUS_REMINDER = "list_status"
        val STATUS_NOTIFICATION = "n_status"
        val MELODY = "melody"
        val RADIUS = "radius"
        val LED_COLOR = "led_color"
        val WIDGET_ID = "widget_id"
        val MARKER = "marker"
        val NAME = "place_name"

        private val DB_NAME = "moove_db"
        private val DB_VERSION = 1
        private val CURRENT_TABLE_NAME = "moove_table"
        private val LOCATION_TABLE_NAME = "locations_table"
        private var mContext: Context

        private val CURRENT_TABLE_CREATE = "create table " + CURRENT_TABLE_NAME + "(" +
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
                WIDGET_ID + " VARCHAR(255), " +
                MARKER + " INTEGER, " +
                LATITUDE + " REAL, " +
                LONGITUDE + " REAL, " +
                UUID + " VARCHAR(255)" +
                ");"

        private val LOCATION_TABLE_CREATE = "create table " + LOCATION_TABLE_NAME + "(" +
                _ID + " integer primary key autoincrement, " +
                NAME + " VARCHAR(255), " +
                LATITUDE + " REAL, " +
                LONGITUDE + " REAL" +
                ");"
    }
}