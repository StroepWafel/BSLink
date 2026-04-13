package com.beastsaber.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlist_maps ORDER BY sortOrder ASC, mapId ASC")
    fun observeAll(): Flow<List<PlaylistMapEntity>>

    @Query("SELECT * FROM playlist_maps ORDER BY sortOrder ASC, mapId ASC")
    suspend fun getAll(): List<PlaylistMapEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_maps WHERE mapId = :mapId LIMIT 1)")
    fun observeContains(mapId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PlaylistMapEntity)

    @Delete
    suspend fun delete(entity: PlaylistMapEntity)

    @Query("DELETE FROM playlist_maps WHERE mapId = :mapId")
    suspend fun deleteByMapId(mapId: String)

    @Query("DELETE FROM playlist_maps")
    suspend fun clear()
}
