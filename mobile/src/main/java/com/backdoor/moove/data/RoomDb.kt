package com.backdoor.moove.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.backdoor.moove.data.dao.PlaceDao
import com.backdoor.moove.data.dao.ReminderDao
import com.backdoor.moove.utils.launchDefault

@Database(entities = [Place::class, Reminder::class],
        version = 1,
        exportSchema = false)
abstract class RoomDb : RoomDatabase() {

    abstract fun placeDao(): PlaceDao

    abstract fun reminderDao(): ReminderDao

    companion object {
        private const val DB_NAME = "app_db"
        private var INSTANCE: RoomDb? = null

        fun getInMemoryDatabase(context: Context): RoomDb {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.applicationContext, RoomDb::class.java, DB_NAME)
                        .build()
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

    fun cleanDb() {
        launchDefault {
            placeDao().deleteAll()
            reminderDao().deleteAll()
        }
    }
}