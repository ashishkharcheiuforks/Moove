package com.backdoor.moove.core.helper

import android.content.Context
import android.database.Cursor
import android.os.AsyncTask

import com.backdoor.moove.core.utils.LocationUtil
import com.google.android.gms.maps.model.LatLng

/**
 * Copyright 2015 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
object Place {

    fun addPlace(context: Context, latLng: LatLng) {
        class Address(private val mContext: Context) : AsyncTask<Double, Void, Void>() {

            protected override fun doInBackground(vararg params: Double): Void? {
                val latitude = params[0]
                val longitude = params[1]
                val db = DataBase(mContext)
                db.open()
                val c = db.getPlace(latitude, longitude)
                if (c == null || !c.moveToFirst()) {
                    val name = LocationUtil.getAddress(mContext, latitude, longitude)
                    db.insertPlace(name, latitude, longitude)
                } else {
                    c.close()
                }
                db.close()
                return null
            }
        }

        Address(context).execute(latLng.latitude, latLng.longitude)
    }
}
