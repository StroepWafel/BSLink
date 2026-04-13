package com.beastsaber.app.ui.screens.send

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beastsaber.app.data.export.playlistToExportJson
import com.beastsaber.app.data.repo.PlaylistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class SendUiState(
    val loading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class SendToPcViewModel(
    private val playlist: PlaylistRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SendUiState())
    val state: StateFlow<SendUiState> = _state.asStateFlow()

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    fun send(baseUrl: String, token: String, autoDownload: Boolean) {
        viewModelScope.launch {
            _state.value = SendUiState(loading = true)
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val json = playlistToExportJson(playlist.getAll())
                    val trimmed = baseUrl.trim().trimEnd('/')
                    val enc = URLEncoder.encode(token, Charsets.UTF_8.name())
                    val auto = if (autoDownload) "1" else "0"
                    val url = "$trimmed/import?token=$enc&autoDownload=$auto"
                    val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
                    val req = Request.Builder().url(url).post(body).build()
                    val resp = http.newCall(req).execute()
                    if (!resp.isSuccessful) {
                        error("HTTP ${resp.code}: ${resp.message}")
                    }
                }
            }
            result.onSuccess {
                _state.value = SendUiState(message = "Sent to PC")
            }.onFailure { e ->
                _state.value = SendUiState(error = e.message ?: "Failed")
            }
        }
    }

    fun clearFeedback() {
        _state.value = SendUiState()
    }

    fun setQrError(message: String) {
        _state.value = SendUiState(error = message)
    }
}
