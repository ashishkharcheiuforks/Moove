package com.backdoor.moove.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.backdoor.moove.data.Place

@Dao
interface PlaceDao {
    @Transaction
    @Query("select * from Place order by createdAt DESC")
    fun loadAll(): LiveData<List<Place>>

    @Transaction
    @Query("select * from Place order by createdAt DESC")
    suspend fun getAll(): List<Place>

    @Query("select * from Place where uuId = :uuId")
    fun loadById(uuId: String): LiveData<Place>

    @Query("select * from Place where uuId = :uuId")
    suspend fun getById(uuId: String): Place?

    @Insert(onConflict = REPLACE)
    suspend fun insert(place: Place)

    @Delete
    suspend fun delete(place: Place)

    @Query("DELETE FROM Place")
    suspend fun deleteAll()
}