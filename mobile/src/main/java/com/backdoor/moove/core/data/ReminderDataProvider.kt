package com.backdoor.moove.core.data

import android.content.Context
import android.database.Cursor

import com.backdoor.moove.core.helper.DataBase

import java.util.ArrayList

object ReminderDataProvider {

    fun getListData(context: Context): ArrayList<MarkerModel> {
        val list = ArrayList<MarkerModel>()
        list.clear()
        val db = DataBase(context)
        db.open()
        val c = db.allReminders
        if (c != null && c.moveToFirst()) {
            do {
                val title = c.getString(c.getColumnIndex(DataBase.SUMMARY))
                val id = c.getLong(c.getColumnIndex(DataBase._ID))
                var icon = c.getInt(c.getColumnIndex(DataBase.MARKER))

                if (icon == -1) {
                    icon = 0
                }

                list.add(MarkerModel(title, id, icon))
            } while (c.moveToNext())
        }
        c?.close()

        db.close()
        return list
    }

    fun load(context: Context): ArrayList<ReminderModel> {
        val list = ArrayList<ReminderModel>()
        val db = DataBase(context)
        db.open()
        val c = db.allReminders
        if (c != null && c.moveToFirst()) {
            do {
                val title = c.getString(c.getColumnIndex(DataBase.SUMMARY))
                val type = c.getString(c.getColumnIndex(DataBase.TYPE))
                val number = c.getString(c.getColumnIndex(DataBase.NUMBER))
                val uuID = c.getString(c.getColumnIndex(DataBase.UUID))
                val melody = c.getString(c.getColumnIndex(DataBase.MELODY))
                val startTime = c.getLong(c.getColumnIndex(DataBase.START_TIME))
                val id = c.getLong(c.getColumnIndex(DataBase._ID))
                val statusDb = c.getInt(c.getColumnIndex(DataBase.STATUS_DB))
                val statusList = c.getInt(c.getColumnIndex(DataBase.STATUS_REMINDER))
                val lat = c.getDouble(c.getColumnIndex(DataBase.LATITUDE))
                val lon = c.getDouble(c.getColumnIndex(DataBase.LONGITUDE))
                val radius = c.getInt(c.getColumnIndex(DataBase.RADIUS))

                list.add(ReminderModel(title, type, uuID, statusDb, startTime, id,
                        doubleArrayOf(lat, lon), number, statusList, radius, melody))
            } while (c.moveToNext())
        }
        c?.close()

        db.close()
        return list
    }

    fun getItem(mContext: Context, id: Long): ReminderModel? {
        var item: ReminderModel? = null
        val db = DataBase(mContext)
        db.open()
        val c = db.getReminder(id)
        if (c != null && c.moveToFirst()) {
            val title = c.getString(c.getColumnIndex(DataBase.SUMMARY))
            val type = c.getString(c.getColumnIndex(DataBase.TYPE))
            val number = c.getString(c.getColumnIndex(DataBase.NUMBER))
            val uuID = c.getString(c.getColumnIndex(DataBase.UUID))
            val melody = c.getString(c.getColumnIndex(DataBase.MELODY))
            val startTime = c.getLong(c.getColumnIndex(DataBase.START_TIME))
            val statusDb = c.getInt(c.getColumnIndex(DataBase.STATUS_DB))
            val statusList = c.getInt(c.getColumnIndex(DataBase.STATUS_REMINDER))
            val lat = c.getDouble(c.getColumnIndex(DataBase.LATITUDE))
            val lon = c.getDouble(c.getColumnIndex(DataBase.LONGITUDE))
            val radius = c.getInt(c.getColumnIndex(DataBase.RADIUS))

            item = ReminderModel(title, type, uuID, statusDb, startTime, id,
                    doubleArrayOf(lat, lon), number, statusList, radius, melody)
        }
        c?.close()

        db.close()
        return item
    }
}
