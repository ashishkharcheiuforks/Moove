package com.backdoor.moove.core.helper;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.backdoor.moove.core.utils.LocationUtil;
import com.google.android.gms.maps.model.LatLng;

/**
 * Copyright 2015 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class Place {

    public static void addPlace(Context context, LatLng latLng) {
        class Address extends AsyncTask<Double, Void, Void> {

            private Context mContext;

            Address(Context context1) {
                this.mContext = context1;
            }

            @Override
            protected Void doInBackground(Double... params) {
                double latitude = params[0];
                double longitude = params[1];
                DataBase db = new DataBase(mContext);
                db.open();
                Cursor c = db.getPlace(latitude, longitude);
                if (c == null || !c.moveToFirst()) {
                    String name = LocationUtil.getAddress(mContext, latitude, longitude);
                    db.insertPlace(name, latitude, longitude);
                } else {
                    c.close();
                }
                db.close();
                return null;
            }
        }

        new Address(context).execute(latLng.latitude, latLng.longitude);
    }
}
