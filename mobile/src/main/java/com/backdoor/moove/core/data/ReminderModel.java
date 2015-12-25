package com.backdoor.moove.core.data;

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
public class ReminderModel {
    private final String title, type, uuId, number, melody;
    private final int status, statusDb, radius;
    private final long startTime, id;
    private final double[] place;

    public ReminderModel(String title, String type, String uuId, int status,
                         long startTime, long id, double[] place, String number,
                         int statusDb, int radius, String melody){
        this.title = title;
        this.type = type;
        this.startTime = startTime;
        this.id = id;
        this.status = status;
        this.uuId = uuId;
        this.place = place;
        this.number = number;
        this.statusDb = statusDb;
        this.radius = radius;
        this.melody = melody;
    }

    public String getMelody() {
        return melody;
    }

    public int getRadius() {
        return radius;
    }

    public int getStatusDb(){
        return statusDb;
    }

    public int getStatus(){
        return status;
    }

    public double[] getPlace(){
        return place;
    }

    public long getStartTime(){
        return startTime;
    }

    public long getId(){
        return id;
    }

    public String getTitle(){
        return title;
    }

    public String getType(){
        return type;
    }

    public String getUuId(){
        return uuId;
    }

    public String getNumber(){
        return number;
    }
}
