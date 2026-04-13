package com.beastsaber.app.data.repo

import com.beastsaber.app.data.filters.FilterOptionKind

/**
 * Shared BeatSaver map filters (search + browse). Edit here to change filter defaults and wiring.
 */
data class MapFilterFormState(
    val curated: Boolean? = null,
    val verified: Boolean? = null,
    val fullSpread: Boolean? = null,
    val automapper: AutomapperFilter = AutomapperFilter.HumanOnly,
    val leaderboard: LeaderboardFilter = LeaderboardFilter.All,
    val minNps: String? = null,
    val maxNps: String? = null,
    val minBpm: String? = null,
    val maxBpm: String? = null,
    val selectedTagSlugs: Set<String> = emptySet(),
    val selectedEnvironmentIds: Set<String> = emptySet(),
    val chroma: Boolean? = null,
    val noodle: Boolean? = null,
    val me: Boolean? = null,
    val cinema: Boolean? = null,
    val vivify: Boolean? = null,
    val datePreset: DatePreset = DatePreset.None
) {
    /** True when any filter beyond the default “open browse” baseline is set (ignores sort order). */
    fun hasNonDefaultFilters(): Boolean =
        curated != null ||
            verified != null ||
            fullSpread != null ||
            automapper != AutomapperFilter.HumanOnly ||
            leaderboard != LeaderboardFilter.All ||
            minNps != null ||
            maxNps != null ||
            minBpm != null ||
            maxBpm != null ||
            selectedTagSlugs.isNotEmpty() ||
            selectedEnvironmentIds.isNotEmpty() ||
            chroma != null ||
            noodle != null ||
            me != null ||
            cinema != null ||
            vivify != null ||
            datePreset != DatePreset.None

    fun activeFilterCount(): Int {
        var n = 0
        if (curated != null) n++
        if (verified != null) n++
        if (fullSpread != null) n++
        if (automapper != AutomapperFilter.HumanOnly) n++
        if (leaderboard != LeaderboardFilter.All) n++
        if (minNps != null) n++
        if (maxNps != null) n++
        if (minBpm != null) n++
        if (maxBpm != null) n++
        n += selectedTagSlugs.size
        n += selectedEnvironmentIds.size
        if (chroma != null) n++
        if (noodle != null) n++
        if (me != null) n++
        if (cinema != null) n++
        if (vivify != null) n++
        if (datePreset != DatePreset.None) n++
        return n
    }

    fun toSearchFilters(query: String, order: SearchSortOrder): SearchFilters =
        SearchFilters(
            query = query,
            order = order,
            curated = curated,
            verified = verified,
            fullSpread = fullSpread,
            automapper = automapper,
            leaderboard = leaderboard,
            minNps = minNps,
            maxNps = maxNps,
            minBpm = minBpm,
            maxBpm = maxBpm,
            tagSlugs = selectedTagSlugs,
            environmentIds = selectedEnvironmentIds,
            chroma = chroma,
            noodle = noodle,
            me = me,
            cinema = cinema,
            vivify = vivify,
            datePreset = datePreset
        )

    companion object {
        fun cleared() = MapFilterFormState()
    }
}

fun MapFilterFormState.toggledCatalogOption(kind: FilterOptionKind, apiValue: String): MapFilterFormState =
    when (kind) {
        FilterOptionKind.Tag -> {
            val next = selectedTagSlugs.toMutableSet()
            if (!next.add(apiValue)) next.remove(apiValue)
            copy(selectedTagSlugs = next)
        }
        FilterOptionKind.Environment -> {
            val next = selectedEnvironmentIds.toMutableSet()
            if (!next.add(apiValue)) next.remove(apiValue)
            copy(selectedEnvironmentIds = next)
        }
    }
