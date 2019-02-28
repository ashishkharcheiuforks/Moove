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
    fun getAll(): List<Place>

    @Query("select * from Place where uuId = :uuId")
    fun loadById(uuId: String): LiveData<Place>

    @Query("select * from Place where uuId = :uuId")
    fun getById(uuId: String): Place?

    @Query("select * from Place where latitude = :lat and longitude = :lon limit 1")
    fun getByCoord(lat: Double, lon: Double): Place?

    @Insert(onConflict = REPLACE)
    fun insert(place: Place)

    @Delete
    fun delete(place: Place)

    @Query("DELETE FROM Place")
    fun deleteAll()
}