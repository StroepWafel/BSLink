package com.beastsaber.app.ui.screens.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beastsaber.app.data.model.MapDetail
import com.beastsaber.app.data.repo.BeatSaverRepository
import com.beastsaber.app.data.repo.PlaylistRepository
import com.beastsaber.app.data.repo.LatestFeedSort
import com.beastsaber.app.data.repo.MapFilterFormState
import com.beastsaber.app.data.repo.toSearchSortOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class BrowseUiState(
    val feedSort: LatestFeedSort = LatestFeedSort.LastPublished,
    val filters: MapFilterFormState = MapFilterFormState(),
    val items: List<MapDetail> = emptyList(),
    val page: Int = 0,
    val loading: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
    /** `true` added, `false` failed; cleared after snackbar */
    val playlistAddResult: Boolean? = null
)

class BrowseViewModel(
    private val repo: BeatSaverRepository,
    private val playlist: PlaylistRepository
) : ViewModel() {

    private val loadMoreMutex = Mutex()

    private val _state = MutableStateFlow(BrowseUiState())
    val state: StateFlow<BrowseUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun addToPlaylist(map: MapDetail) {
        viewModelScope.launch {
            val ok = playlist.addFromMapDetail(map)
            _state.value = _state.value.copy(playlistAddResult = ok)
        }
    }

    fun clearPlaylistAddResult() {
        _state.value = _state.value.copy(playlistAddResult = null)
    }

    private fun useSearchApi(s: BrowseUiState): Boolean =
        s.filters.hasNonDefaultFilters() || s.feedSort == LatestFeedSort.Rating

    fun setFeedSort(sort: LatestFeedSort) {
        val s = _state.value
        if (s.feedSort == sort) return
        _state.value = s.copy(feedSort = sort, loading = true, error = null)
        viewModelScope.launch {
            loadFirstPage(_state.value)
        }
    }

    /** Commit filters from the sheet and reload the list. */
    fun applyFiltersFromSheet(filters: MapFilterFormState) {
        _state.value = _state.value.copy(filters = filters)
        viewModelScope.launch {
            loadFirstPage(_state.value)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            loadFirstPage(_state.value)
        }
    }

    private suspend fun loadFirstPage(s: BrowseUiState) {
        if (useSearchApi(s)) {
            val sf = s.filters.toSearchFilters(query = "", order = s.feedSort.toSearchSortOrder())
            runCatching { repo.searchMaps(0, sf) }
                .onSuccess { resp ->
                    val docs = resp.docs.orEmpty()
                    _state.value = s.copy(
                        items = docs,
                        page = 0,
                        loading = false,
                        endReached = docs.isEmpty()
                    )
                }
                .onFailure { e ->
                    _state.value = s.copy(loading = false, error = e.message ?: "Error")
                }
        } else {
            runCatching { repo.latestMaps(0, s.feedSort) }
                .onSuccess { resp ->
                    val docs = resp.docs.orEmpty()
                    _state.value = s.copy(
                        items = docs,
                        page = 0,
                        loading = false,
                        endReached = docs.isEmpty()
                    )
                }
                .onFailure { e ->
                    _state.value = s.copy(
                        items = emptyList(),
                        loading = false,
                        error = e.message ?: "Error"
                    )
                }
        }
    }

    fun loadMore() {
        viewModelScope.launch {
            loadMoreMutex.withLock {
                val s = _state.value
                if (s.loading || s.endReached) return@withLock
                val next = s.page + 1
                _state.value = s.copy(loading = true, error = null)
                if (useSearchApi(s)) {
                    val sf = s.filters.toSearchFilters(query = "", order = s.feedSort.toSearchSortOrder())
                    runCatching { repo.searchMaps(next, sf) }
                        .onSuccess { resp ->
                            val docs = resp.docs.orEmpty()
                            val merged = mergeAppendDedup(s.items, docs)
                            val added = merged.size - s.items.size
                            _state.value = s.copy(
                                items = merged,
                                page = next,
                                loading = false,
                                endReached = docs.isEmpty() || (docs.isNotEmpty() && added == 0)
                            )
                        }
                        .onFailure { e ->
                            _state.value = s.copy(loading = false, error = e.message ?: "Error")
                        }
                } else {
                    runCatching { repo.latestMaps(next, s.feedSort) }
                        .onSuccess { resp ->
                            val docs = resp.docs.orEmpty()
                            val merged = mergeAppendDedup(s.items, docs)
                            val added = merged.size - s.items.size
                            _state.value = s.copy(
                                items = merged,
                                page = next,
                                loading = false,
                                endReached = docs.isEmpty() || (docs.isNotEmpty() && added == 0)
                            )
                        }
                        .onFailure { e ->
                            _state.value = s.copy(loading = false, error = e.message ?: "Error")
                        }
                }
            }
        }
    }
}

private fun mergeAppendDedup(existing: List<MapDetail>, page: List<MapDetail>): List<MapDetail> {
    val seen = existing.map { it.id }.toHashSet()
    return existing + page.filter { seen.add(it.id) }
}
