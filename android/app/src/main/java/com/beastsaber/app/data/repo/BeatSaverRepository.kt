package com.beastsaber.app.data.repo

import com.beastsaber.app.data.model.MapDetail
import com.beastsaber.app.data.network.BeatSaverApi

class BeatSaverRepository(private val api: BeatSaverApi) {

    suspend fun latestMaps(
        page: Int,
        sort: LatestFeedSort = LatestFeedSort.LastPublished,
        pageSize: Int = 40
    ) = api.mapsLatest(
        page = page,
        pageSize = pageSize,
        sort = sort.apiValue,
        automapper = null
    )

    suspend fun searchMaps(page: Int, filters: SearchFilters) =
        api.searchText(page, filters.toQueryMap())

    suspend fun mapById(id: String): MapDetail = api.mapById(id)
}
