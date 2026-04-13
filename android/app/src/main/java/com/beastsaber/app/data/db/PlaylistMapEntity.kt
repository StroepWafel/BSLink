package com.beastsaber.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist_maps")
data class PlaylistMapEntity(
    @PrimaryKey val mapId: String,
    val hash: String,
    val songName: String,
    val songSubName: String?,
    val levelAuthorName: String,
    val downloadURL: String,
    val coverURL: String?,
    val sortOrder: Long
)
