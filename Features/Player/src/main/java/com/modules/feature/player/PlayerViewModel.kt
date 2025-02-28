package com.modules.feature.player

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modules.core.datastore.repository.LocalPreferencesRepository
import com.modules.core.datastore.repository.PlayerPreferencesRepository
import com.modules.core.model.Video
import com.modules.core.model.VideoState
import com.modules.core.model.VideoZoom
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val preferencesRepository: PlayerPreferencesRepository,
    private val localPlayerPreferencesRepository: LocalPreferencesRepository,
    private val getSortedPlaylistUseCase: GetSortedPlaylistUseCase,
) : ViewModel() {

    var playWhenReady: Boolean = true
    var skipSilenceEnabled: Boolean = false

    val playerPrefs = preferencesRepository.playerPreferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = runBlocking { preferencesRepository.playerPreferences.first() },
    )

    val appPrefs = localPlayerPreferencesRepository.applicationPreferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = runBlocking { localPlayerPreferencesRepository.applicationPreferences.first() },
    )

    suspend fun getPlaylistFromUri(uri: Uri): List<Video> {
        return getSortedPlaylistUseCase.invoke(uri)
    }

    suspend fun getVideoState(uri: String): VideoState? {
        return mediaRepository.getVideoState(uri)
    }

    fun updateMediumZoom(uri: String, zoom: Float) {
        mediaRepository.updateMediumZoom(uri, zoom)
    }

    fun setPlayerBrightness(value: Float) {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences { it.copy(playerBrightness = value) }
        }
    }

    fun setVideoZoom(videoZoom: VideoZoom) {
        viewModelScope.launch {
            preferencesRepository.updatePlayerPreferences { it.copy(playerVideoZoom = videoZoom) }
        }
    }
}
