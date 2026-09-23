package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {
    @Query("SELECT * FROM trackers ORDER BY createdAt DESC")
    fun getAllTrackers(): Flow<List<TrackerEntity>>

    @Query("SELECT * FROM trackers WHERE id = :id LIMIT 1")
    suspend fun getTrackerById(id: String): TrackerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracker(tracker: TrackerEntity)

    @Delete
    suspend fun deleteTracker(tracker: TrackerEntity)

    @Query("DELETE FROM trackers WHERE id = :id")
    suspend fun deleteTrackerById(id: String)
}
