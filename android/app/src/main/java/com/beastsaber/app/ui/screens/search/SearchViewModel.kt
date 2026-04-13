package com.beastsaber.app.ui.screens.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.beastsaber.app.R
import com.beastsaber.app.data.model.MapDetail
import com.beastsaber.app.data.repo.BeatSaverRepository
import com.beastsaber.app.data.repo.PlaylistRepository
import com.beastsaber.app.data.repo.MapFilterFormState
import com.beastsaber.app.data.repo.SearchFilters
import com.beastsaber.app.data.repo.SearchSortOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class SearchUiState(
    val query: String = "",
    val order: SearchSortOrder = SearchSortOrder.Relevance,
    val filters: MapFilterFormState = MapFilterFormState(),
    val items: List<MapDetail> = emptyList(),
    val page: Int = 0,
    val loading: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
    val searched: Boolean = false,
    val validationHint: String? = null,
    val playlistAddResult: Boolean? = null
)

class SearchViewModel(
    application: Application,
    private val repo: BeatSaverRepository,
    private val playlist: PlaylistRepository
) : AndroidViewModel(application) {

    private val loadMoreMutex = Mutex()

    /** Snapshot of filters used for the last successful `search()`; required so load-more keeps paging even if the user edits the query field. */
    private var lastSearchFilters: SearchFilters? = null

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    fun setQuery(q: String) {
        _state.value = _state.value.copy(query = q, validationHint = null)
    }

    fun clearValidationHint() {
        _state.value = _state.value.copy(validationHint = null)
    }

    fun setOrder(o: SearchSortOrder) {
        _state.value = _state.value.copy(order = o)
    }

    fun replaceFilters(f: MapFilterFormState) {
        _state.value = _state.value.copy(filters = f)
    }

    fun clearFilters() {
        _state.value = _state.value.copy(
            order = SearchSortOrder.Relevance,
            filters = MapFilterFormState.cleared()
        )
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

    private fun buildFilters() =
        _state.value.filters.toSearchFilters(query = _state.value.query.trim(), order = _state.value.order)

    fun search() {
        val filters = buildFilters()
        if (!filters.query.isNotBlank() && !filters.hasStructuralFilters()) {
            _state.value = _state.value.copy(
                validationHint = getApplication<Application>().getString(R.string.search_needs_query_or_filters)
            )
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(
                loading = true,
                searched = true,
                validationHint = null,
                error = null
            )
            runCatching { repo.searchMaps(0, filters) }
                .onSuccess { resp ->
                    val docs = resp.docs.orEmpty()
                    lastSearchFilters = filters
                    _state.value = _state.value.copy(
                        items = docs,
                        page = 0,
                        loading = false,
                        endReached = docs.isEmpty(),
                        searched = true
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = e.message ?: "Error",
                        searched = true
                    )
                }
        }
    }

    fun loadMore() {
        viewModelScope.launch {
            loadMoreMutex.withLock {
                val s = _state.value
                if (s.loading || s.endReached || !s.searched) return@withLock
                val filters = lastSearchFilters ?: return@withLock
                val next = s.page + 1
                _state.value = s.copy(loading = true, error = null)
                runCatching { repo.searchMaps(next, filters) }
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

private fun mergeAppendDedup(existing: List<MapDetail>, page: List<MapDetail>): List<MapDetail> {
    val seen = existing.map { it.id }.toHashSet()
    return existing + page.filter { seen.add(it.id) }
}
