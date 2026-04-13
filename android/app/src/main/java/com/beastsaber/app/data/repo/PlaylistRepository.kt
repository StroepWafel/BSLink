package com.beastsaber.app.data.repo

import com.beastsaber.app.data.db.PlaylistDao
import com.beastsaber.app.data.db.PlaylistMapEntity
import com.beastsaber.app.data.export.mapDetailToExportEntry
import com.beastsaber.app.data.model.MapDetail
import com.beastsaber.app.data.model.primaryVersion
import kotlinx.coroutines.flow.Flow

class PlaylistRepository(private val dao: PlaylistDao) {

    fun observePlaylist(): Flow<List<PlaylistMapEntity>> = dao.observeAll()

    fun observeContains(mapId: String): Flow<Boolean> = dao.observeContains(mapId)

    suspend fun addFromMapDetail(map: MapDetail): Boolean {
        val v = map.primaryVersion() ?: return false
        val entry = mapDetailToExportEntry(map, v) ?: return false
        val entity = PlaylistMapEntity(
            mapId = map.id,
            hash = entry.hash,
            songName = entry.songName,
            songSubName = entry.songSubName,
            levelAuthorName = entry.levelAuthorName,
            downloadURL = entry.downloadURL,
            coverURL = entry.coverURL,
            sortOrder = System.nanoTime()
        )
        dao.upsert(entity)
        return true
    }

    suspend fun remove(mapId: String) = dao.deleteByMapId(mapId)

    suspend fun getAll(): List<PlaylistMapEntity> = dao.getAll()
}
