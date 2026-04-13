package com.beastsaber.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PlaylistMapEntity::class], version = 1, exportSchema = false)
abstract class PlaylistDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao

    companion object {
        fun get(context: Context): PlaylistDatabase =
            Room.databaseBuilder(context, PlaylistDatabase::class.java, "playlist.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
