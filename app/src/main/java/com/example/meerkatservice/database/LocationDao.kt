package com.example.meerkatservice.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert
    suspend fun insert(locationEntities: List<LocationEntity>)

    @Delete
    suspend fun delete(locationEntity: LocationEntity)

    @Query("SELECT * FROM location_entity ORDER BY time")
    fun getAllFlow(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM location_entity ORDER BY time DESC LIMIT :count")
    fun getLatestFlow(count: Int): Flow<List<LocationEntity>>

    @Query("SELECT COUNT(*) FROM location_entity")
    fun getCount(): Flow<Int>
}
