package com.backdoor.moove.core.data

import android.content.Context
import android.database.Cursor

import com.backdoor.moove.core.consts.Constants
import com.backdoor.moove.core.consts.Prefs
import com.backdoor.moove.core.helper.DataBase
import com.backdoor.moove.core.helper.SharedPrefs
import com.google.android.gms.maps.model.LatLng

import java.util.ArrayList

class PlaceDataProvider(private val mContext: Context, list: Boolean) {
    private val data: MutableList<MarkerModel>?

    val count: Int
        get() = data?.size ?: 0

    init {
        data = ArrayList()
        if (list) {
            loadPlaces()
        } else {
            loadReminders()
        }
    }

    fun getData(): List<MarkerModel>? {
        return data
    }

    fun getItem(index: Int): MarkerModel? {
        return if (index < 0 || index >= count) {
            null
        } else data!![index]

    }

    private fun loadReminders() {
        data!!.clear()
        val db = DataBase(mContext)
        db.open()
        val c = db.getReminders(Constants.ENABLE)
        if (c != null && c.moveToNext()) {
            do {
                val text = c.getString(c.getColumnIndex(DataBase.SUMMARY))
                val id = c.getLong(c.getColumnIndex(DataBase._ID))
                val latitude = c.getDouble(c.getColumnIndex(DataBase.LATITUDE))
                val longitude = c.getDouble(c.getColumnIndex(DataBase.LONGITUDE))
                val style = c.getInt(c.getColumnIndex(DataBase.MARKER))
                var radius = c.getInt(c.getColumnIndex(DataBase.RADIUS))
                if (radius == -1) {
                    radius = SharedPrefs.getInstance(mContext)!!.loadInt(Prefs.LOCATION_RADIUS)
                }
                data.add(MarkerModel(text, LatLng(latitude, longitude), style, id, radius))
            } while (c.moveToNext())
        }
        c?.close()
        db.close()
    }

    fun loadPlaces() {
        data!!.clear()
        val db = DataBase(mContext)
        db.open()
        val c = db.queryPlaces()
        if (c != null && c.moveToNext()) {
            do {
                val text = c.getString(c.getColumnIndex(DataBase.NAME))
                val id = c.getLong(c.getColumnIndex(DataBase._ID))
                data.add(MarkerModel(text, id))
            } while (c.moveToNext())
        }
        c?.close()
        db.close()
    }
}
