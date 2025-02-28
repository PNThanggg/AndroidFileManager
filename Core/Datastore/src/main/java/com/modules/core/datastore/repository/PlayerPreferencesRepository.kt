package com.modules.core.datastore.repository

import com.modules.core.datastore.datasource.PlayerPreferencesDataSource
import com.modules.core.datastore.models.PlayerPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlayerPreferencesRepository @Inject constructor(
    private val playerPreferencesDataSource: PlayerPreferencesDataSource,
) : IPlayerPreferencesRepository {
    override val playerPreferences: Flow<PlayerPreferences>
        get() = playerPreferencesDataSource.preferences

    override suspend fun updatePlayerPreferences(
        transform: suspend (PlayerPreferences) -> PlayerPreferences
    ) {
        playerPreferencesDataSource.update(transform)
    }
}
