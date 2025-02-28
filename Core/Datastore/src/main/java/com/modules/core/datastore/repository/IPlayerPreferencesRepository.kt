package com.modules.core.datastore.repository

import com.modules.core.datastore.models.PlayerPreferences
import kotlinx.coroutines.flow.Flow

interface IPlayerPreferencesRepository {

    /**
     * Stream of [PlayerPreferences].
     */
    val playerPreferences: Flow<PlayerPreferences>


    suspend fun updatePlayerPreferences(
        transform: suspend (PlayerPreferences) -> PlayerPreferences,
    )
}
