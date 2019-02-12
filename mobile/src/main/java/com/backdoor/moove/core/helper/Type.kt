package com.backdoor.moove.core.helper

import android.content.Context
import android.database.Cursor

open class Type(private val mContext: Context) {
    /**
     * Get reminder type.
     *
     * @return reminder type
     */
    /**
     * Set reminder type.
     *
     * @param type reminder type.
     */
    var type: String? = null

    init {
        this.type = ""
    }

    /**
     * Get reminder object.
     *
     * @param id reminder identifier.
     * @return reminder object
     */
    fun getItem(id: Long): Reminder? {
        val db = DataBase(mContext)
        db.open()
        val c = db.getReminder(id)
        if (c != null && c.moveToFirst()) {
            val summary = c.getString(c.getColumnIndex(DataBase.SUMMARY))
            val number = c.getString(c.getColumnIndex(DataBase.NUMBER))
            val due = c.getLong(c.getColumnIndex(DataBase.START_TIME))
            val type = c.getString(c.getColumnIndex(DataBase.TYPE))
            val radius = c.getInt(c.getColumnIndex(DataBase.RADIUS))
            val ledColor = c.getInt(c.getColumnIndex(DataBase.LED_COLOR))
            val marker = c.getInt(c.getColumnIndex(DataBase.MARKER))
            val volume = c.getInt(c.getColumnIndex(DataBase.VOLUME))
            val melody = c.getString(c.getColumnIndex(DataBase.MELODY))
            val uuId = c.getString(c.getColumnIndex(DataBase.UUID))
            val latitude = c.getDouble(c.getColumnIndex(DataBase.LATITUDE))
            val longitude = c.getDouble(c.getColumnIndex(DataBase.LONGITUDE))

            c.close()
            db.close()

            return Reminder(id, summary, type, melody, uuId,
                    doubleArrayOf(latitude, longitude), number,
                    radius, due, ledColor, marker, volume)
        } else
            return null
    }

    /**
     * Save new reminder to database.
     *
     * @param item reminder object.
     * @return reminder identifier
     */
    open fun save(item: Reminder): Long {
        val db = DataBase(mContext)
        db.open()
        val id = db.insertReminder(item.title, item.type,
                item.number, item.startTime, item.place!![0],
                item.place!![1], item.uuId, item.melody,
                item.radius, item.color, item.marker, item.volume)
        db.close()
        return id
    }

    /**
     * Update reminder in database.
     *
     * @param id   reminder identifier.
     * @param item reminder object.
     */
    open fun save(id: Long, item: Reminder) {
        val db = DataBase(mContext)
        db.open()
        db.updateReminder(id, item.title, item.type, item.number,
                item.startTime, item.place!![0], item.place!![1],
                item.melody, item.radius, item.color, item.marker,
                item.volume)
        db.close()
    }
}
