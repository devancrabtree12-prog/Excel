package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlueprintDao {
    @Query("SELECT * FROM blueprints ORDER BY isPrebuilt DESC, createdAt DESC")
    fun getAllBlueprints(): Flow<List<BlueprintEntity>>

    @Query("SELECT * FROM blueprints WHERE id = :id LIMIT 1")
    suspend fun getBlueprintById(id: String): BlueprintEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlueprint(blueprint: BlueprintEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(blueprints: List<BlueprintEntity>)

    @Query("DELETE FROM blueprints WHERE id = :id AND isPrebuilt = 0")
    suspend fun deleteCustomBlueprint(id: String)
}
