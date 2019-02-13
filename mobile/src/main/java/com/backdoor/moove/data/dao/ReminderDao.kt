package com.backdoor.moove.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.backdoor.moove.data.Reminder

@Dao
interface ReminderDao {
    @Transaction
    @Query("select * from Reminder order by createdAt DESC")
    fun loadAll(): LiveData<List<Reminder>>

    @Transaction
    @Query("select * from Reminder order by createdAt DESC")
    suspend fun getAll(): List<Reminder>

    @Query("select * from Reminder where uuId = :uuId")
    fun loadById(uuId: String): LiveData<Reminder>

    @Query("select * from Reminder where uuId = :uuId")
    suspend fun getById(uuId: String): Reminder?

    @Insert(onConflict = REPLACE)
    suspend fun insert(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("DELETE FROM Reminder")
    suspend fun deleteAll()
}