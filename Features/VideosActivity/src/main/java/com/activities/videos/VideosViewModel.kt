package com.activities.videos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modules.core.datastore.repository.LocalPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class VideosViewModel @Inject constructor(
    private val localPreferencesRepository: LocalPreferencesRepository,
) : ViewModel() {
    val preferences = localPreferencesRepository.applicationPreferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = runBlocking { localPreferencesRepository.applicationPreferences.first() },
    )
}