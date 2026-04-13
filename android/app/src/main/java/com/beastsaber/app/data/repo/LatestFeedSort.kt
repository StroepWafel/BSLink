package com.beastsaber.app.data.repo

/**
 * `sort` query for [BeatSaverApi.mapsLatest] — matches server [LatestSort].
 */
enum class LatestFeedSort(
    val label: String,
    val apiValue: String
) {
    LastPublished("Newest", "LAST_PUBLISHED"),
    Curated("Curated", "CURATED"),
    Updated("Updated", "UPDATED"),
    /** Uses `/search/text` with `order=Rating` — not supported by `/maps/latest`. */
    Rating("Rating", "RATING")
}

/** When using `/search/text` for filtered browse, map feed intent to Solr sort. */
fun LatestFeedSort.toSearchSortOrder(): SearchSortOrder =
    when (this) {
        LatestFeedSort.LastPublished -> SearchSortOrder.Latest
        LatestFeedSort.Curated -> SearchSortOrder.Curated
        LatestFeedSort.Updated -> SearchSortOrder.Latest
        LatestFeedSort.Rating -> SearchSortOrder.Rating
    }
