package com.activities.videos

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.activities.videos.extensions.loadLocalVideo
import com.module.core.enums.LoadingState
import com.modules.core.datastore.repository.LocalPreferencesRepository
import com.modules.core.model.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

data class VideoActivityState(
    val videos: MutableList<Video> = mutableListOf(),
    val loadingState: LoadingState = LoadingState.Init,
)

@HiltViewModel
class VideosViewModel @Inject constructor(
    private val localPreferencesRepository: LocalPreferencesRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val preferences = localPreferencesRepository.applicationPreferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = runBlocking { localPreferencesRepository.applicationPreferences.first() },
    )

    private var isDataLoaded: Boolean = false

    private val _uiState = MutableStateFlow(VideoActivityState())
    val uiState = _uiState.asStateFlow()

    fun loadListView() {
        viewModelScope.launch {
            if (isDataLoaded) return@launch

            if (_uiState.value.loadingState is LoadingState.Loading) {
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                loadingState = LoadingState.Loading,
            )

            try {
                val videos = context.loadLocalVideo()
                _uiState.value = _uiState.value.copy(
                    videos = videos,
                    loadingState = LoadingState.Success,
                )
                isDataLoaded = true
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    loadingState = LoadingState.Error(message = e.message),
                )
            }
        }
    }
}