package com.beastsaber.app.data.model

import com.google.gson.annotations.SerializedName

data class MapSearchResponse(
    @SerializedName("docs") val docs: List<MapDetail>? = null,
    @SerializedName("info") val info: SearchInfo? = null
)

data class SearchInfo(
    @SerializedName("totalDocs") val totalDocs: Int? = null,
    @SerializedName("pages") val pages: Int? = null
)

data class MapDetail(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("uploader") val uploader: Uploader? = null,
    @SerializedName("metadata") val metadata: MapMetadata? = null,
    @SerializedName("stats") val stats: MapStats? = null,
    @SerializedName("uploaded") val uploaded: String? = null,
    @SerializedName("versions") val versions: List<MapVersion>? = null,
    @SerializedName("tags") val tags: List<String>? = null
)

data class Uploader(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("avatar") val avatar: String? = null
)

data class MapMetadata(
    @SerializedName("songName") val songName: String? = null,
    @SerializedName("songSubName") val songSubName: String? = null,
    @SerializedName("songAuthorName") val songAuthorName: String? = null,
    @SerializedName("levelAuthorName") val levelAuthorName: String? = null,
    @SerializedName("bpm") val bpm: Float? = null,
    @SerializedName("duration") val duration: Int? = null
)

data class MapStats(
    @SerializedName("upvotes") val upvotes: Int? = null,
    @SerializedName("downvotes") val downvotes: Int? = null,
    @SerializedName("score") val score: Float? = null
)

data class MapVersion(
    @SerializedName("hash") val hash: String? = null,
    @SerializedName("key") val key: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("downloadURL") val downloadURL: String? = null,
    @SerializedName("coverURL") val coverURL: String? = null,
    @SerializedName("previewURL") val previewURL: String? = null,
    @SerializedName("diffs") val diffs: List<MapDifficulty>? = null
)

data class MapDifficulty(
    @SerializedName("characteristic") val characteristic: String? = null,
    @SerializedName("difficulty") val difficulty: String? = null,
    @SerializedName("nps") val nps: Float? = null
)

fun MapDetail.primaryVersion(): MapVersion? =
    versions?.firstOrNull { it.state == null || it.state.equals("Published", ignoreCase = true) }
        ?: versions?.firstOrNull()

fun MapDetail.displaySongName(): String =
    metadata?.songName?.takeIf { it.isNotBlank() } ?: name ?: id

fun MapDetail.mapKeyForViewer(): String =
    primaryVersion()?.key?.takeIf { it.isNotBlank() } ?: id
