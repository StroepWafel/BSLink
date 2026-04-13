package com.beastsaber.app.data.repo

import java.time.Instant
import java.time.temporal.ChronoUnit

enum class SearchSortOrder(val apiValue: String) {
    Relevance("Relevance"),
    Latest("Latest"),
    Rating("Rating"),
    Curated("Curated"),
    Random("Random"),
    Duration("Duration")
}

enum class DatePreset(val shortLabel: String) {
    None("Any"),
    Week("7d"),
    Month("30d"),
    Year("365d")
}

/**
 * BeatSaver `/search/text/{page}` query: `automapper=true` includes AI + human maps; omit param for human-only default.
 */
enum class AutomapperFilter {
    /** Send `automapper=true` (include both). */
    All,
    /** Omit param (BeatSaver default excludes AI-heavy catalog). */
    HumanOnly,
    /** Send `automapper=false` (AI-only maps). */
    AiOnly
}

/**
 * Ranked / leaderboard filters. `leaderboard` values match API: BeatSaver / ScoreSaber / BeatLeader.
 */
enum class LeaderboardFilter {
    All,
    /** Any ranked map on either service (`ranked=true`). */
    Ranked,
    ScoreSaber,
    BeatLeader
}

/**
 * Maps to BeatSaver `/search/text/{page}` query parameters (Solr).
 * See https://api.beatsaver.com/docs/ and server search handler.
 */
data class SearchFilters(
    val query: String = "",
    val order: SearchSortOrder = SearchSortOrder.Relevance,
    val curated: Boolean? = null,
    val verified: Boolean? = null,
    val fullSpread: Boolean? = null,
    val automapper: AutomapperFilter = AutomapperFilter.All,
    val leaderboard: LeaderboardFilter = LeaderboardFilter.All,
    val minNps: String? = null,
    val maxNps: String? = null,
    val minBpm: String? = null,
    val maxBpm: String? = null,
    val tagSlugs: Set<String> = emptySet(),
    val environmentIds: Set<String> = emptySet(),
    val chroma: Boolean? = null,
    val noodle: Boolean? = null,
    val me: Boolean? = null,
    val cinema: Boolean? = null,
    val vivify: Boolean? = null,
    val datePreset: DatePreset = DatePreset.None,
    val pageSize: Int = 40
) {
    /**
     * True when the user has chosen something beyond the default browse baseline
     * (HumanOnly matches BeatSaver web omitting `automapper`).
     */
    fun hasStructuralFilters(): Boolean =
        order != SearchSortOrder.Relevance ||
            curated != null ||
            verified != null ||
            fullSpread != null ||
            automapper != AutomapperFilter.HumanOnly ||
            leaderboard != LeaderboardFilter.All ||
            minNps != null ||
            maxNps != null ||
            minBpm != null ||
            maxBpm != null ||
            tagSlugs.isNotEmpty() ||
            environmentIds.isNotEmpty() ||
            chroma != null ||
            noodle != null ||
            me != null ||
            cinema != null ||
            vivify != null ||
            datePreset != DatePreset.None

    fun toQueryMap(): Map<String, String> {
        val m = LinkedHashMap<String, String>()
        val q = query.trim()
        if (q.isNotEmpty()) m["q"] = q
        m["order"] = order.apiValue
        m["pageSize"] = pageSize.coerceIn(1, 100).toString()
        curated?.let { m["curated"] = it.toString() }
        verified?.let { m["verified"] = it.toString() }
        fullSpread?.let { m["fullSpread"] = it.toString() }
        when (automapper) {
            AutomapperFilter.All -> m["automapper"] = "true"
            AutomapperFilter.HumanOnly -> { /* omit */ }
            AutomapperFilter.AiOnly -> m["automapper"] = "false"
        }
        when (leaderboard) {
            LeaderboardFilter.All -> { }
            LeaderboardFilter.Ranked -> m["ranked"] = "true"
            LeaderboardFilter.ScoreSaber -> {
                m["ranked"] = "true"
                m["leaderboard"] = "ScoreSaber"
            }
            LeaderboardFilter.BeatLeader -> m["leaderboard"] = "BeatLeader"
        }
        minNps?.toFloatOrNull()?.let { m["minNps"] = it.toString() }
        maxNps?.toFloatOrNull()?.let { m["maxNps"] = it.toString() }
        minBpm?.toFloatOrNull()?.let { m["minBpm"] = it.toString() }
        maxBpm?.toFloatOrNull()?.let { m["maxBpm"] = it.toString() }
        if (tagSlugs.isNotEmpty()) {
            m["tags"] = tagSlugs.sorted().joinToString(",")
        }
        if (environmentIds.isNotEmpty()) {
            m["environments"] = environmentIds.sorted().joinToString(",")
        }
        chroma?.let { m["chroma"] = it.toString() }
        noodle?.let { m["noodle"] = it.toString() }
        me?.let { m["me"] = it.toString() }
        cinema?.let { m["cinema"] = it.toString() }
        vivify?.let { m["vivify"] = it.toString() }
        val (from, to) = dateRangeIso()
        from?.let { m["from"] = it }
        to?.let { m["to"] = it }
        return m
    }

    private fun dateRangeIso(): Pair<String?, String?> {
        val now = Instant.now()
        val to = now.toString()
        val from = when (datePreset) {
            DatePreset.None -> return null to null
            DatePreset.Week -> now.minus(7, ChronoUnit.DAYS)
            DatePreset.Month -> now.minus(30, ChronoUnit.DAYS)
            DatePreset.Year -> now.minus(365, ChronoUnit.DAYS)
        }
        return from.toString() to to
    }
}
