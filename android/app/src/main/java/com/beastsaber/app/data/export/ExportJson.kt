package com.beastsaber.app.data.export

import com.beastsaber.app.data.db.PlaylistMapEntity
import com.beastsaber.app.data.model.MapDetail
import com.beastsaber.app.data.model.MapVersion
import com.beastsaber.app.data.model.MapMetadata
import com.beastsaber.app.data.model.primaryVersion
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.Instant

private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

fun playlistToExportJson(entities: List<PlaylistMapEntity>): String {
    val root = ExportRoot(
        exportedAt = Instant.now().toString(),
        maps = entities.map { e ->
            ExportMapEntry(
                key = e.mapId,
                hash = e.hash,
                songName = e.songName,
                songSubName = e.songSubName,
                levelAuthorName = e.levelAuthorName,
                downloadURL = e.downloadURL,
                coverURL = e.coverURL
            )
        }
    )
    return gson.toJson(root)
}

fun mapDetailToExportEntry(map: MapDetail, version: MapVersion): ExportMapEntry? {
    val hash = version.hash ?: return null
    val download = version.downloadURL ?: return null
    val meta = map.metadata ?: MapMetadata()
    val key = version.key?.takeIf { it.isNotBlank() } ?: map.id
    return ExportMapEntry(
        key = key,
        hash = hash,
        songName = meta.songName?.takeIf { it.isNotBlank() } ?: map.name ?: map.id,
        songSubName = meta.songSubName,
        levelAuthorName = meta.levelAuthorName.orEmpty(),
        downloadURL = download,
        coverURL = version.coverURL
    )
}
