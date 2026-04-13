package com.beastsaber.app.data.export

import com.google.gson.annotations.SerializedName

data class ExportRoot(
    @SerializedName("format") val format: String = "bsaber-map-list",
    @SerializedName("version") val version: Int = 1,
    @SerializedName("exportedAt") val exportedAt: String,
    @SerializedName("maps") val maps: List<ExportMapEntry>
)

data class ExportMapEntry(
    @SerializedName("key") val key: String,
    @SerializedName("hash") val hash: String,
    @SerializedName("songName") val songName: String,
    @SerializedName("songSubName") val songSubName: String? = null,
    @SerializedName("levelAuthorName") val levelAuthorName: String,
    @SerializedName("downloadURL") val downloadURL: String,
    @SerializedName("coverURL") val coverURL: String? = null
)
