package com.beastsaber.app.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beastsaber.app.data.model.MapDetail
import com.beastsaber.app.data.repo.BeatSaverRepository
import com.beastsaber.app.data.repo.PlaylistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapDetailUiState(
    val loading: Boolean = true,
    val map: MapDetail? = null,
    val error: String? = null,
    val addedMessage: String? = null
)

class MapDetailViewModel(
    private val beatSaver: BeatSaverRepository,
    private val playlist: PlaylistRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MapDetailUiState())
    val state: StateFlow<MapDetailUiState> = _state.asStateFlow()

    fun load(id: String) {
        viewModelScope.launch {
            _state.value = MapDetailUiState(loading = true)
            runCatching { beatSaver.mapById(id) }
                .onSuccess { m -> _state.value = MapDetailUiState(loading = false, map = m) }
                .onFailure { e ->
                    _state.value = MapDetailUiState(loading = false, error = e.message ?: "Error")
                }
        }
    }

    fun addToPlaylist() {
        val m = _state.value.map ?: return
        viewModelScope.launch {
            val ok = playlist.addFromMapDetail(m)
            _state.value = _state.value.copy(
                addedMessage = if (ok) "Saved to My list" else "Could not add (missing files)"
            )
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(addedMessage = null)
    }
}
