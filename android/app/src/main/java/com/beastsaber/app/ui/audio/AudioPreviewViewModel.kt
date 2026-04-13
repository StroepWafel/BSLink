package com.beastsaber.app.ui.audio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AudioPreviewUiState(
    val activeMapId: String? = null,
    val isPlaying: Boolean = false
)

class AudioPreviewViewModel(application: Application) : AndroidViewModel(application) {

    private var player: ExoPlayer? = null

    private val _state = MutableStateFlow(AudioPreviewUiState())
    val state: StateFlow<AudioPreviewUiState> = _state.asStateFlow()

    private fun ensurePlayer(): ExoPlayer {
        player?.let { return it }
        val p = ExoPlayer.Builder(getApplication()).build()
        p.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    _state.value = AudioPreviewUiState()
                }
            }
        })
        player = p
        return p
    }

    fun toggle(mapId: String, url: String?) {
        if (url.isNullOrBlank()) return
        val p = ensurePlayer()
        val current = _state.value
        when {
            current.activeMapId == mapId && p.isPlaying -> p.pause()
            current.activeMapId == mapId && !p.isPlaying -> {
                p.play()
                _state.update { it.copy(isPlaying = true) }
            }
            else -> {
                p.setMediaItem(MediaItem.fromUri(url))
                p.prepare()
                p.play()
                _state.value = AudioPreviewUiState(activeMapId = mapId, isPlaying = true)
            }
        }
    }

    /** Stops playback and clears active preview (navigation away, scrolled off list, etc.). */
    fun stop() {
        player?.let { p ->
            p.pause()
            p.clearMediaItems()
        }
        _state.value = AudioPreviewUiState()
    }

    override fun onCleared() {
        player?.release()
        player = null
        super.onCleared()
    }
}
