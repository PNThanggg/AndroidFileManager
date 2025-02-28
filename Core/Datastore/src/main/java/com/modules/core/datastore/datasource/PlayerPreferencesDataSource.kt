package com.modules.core.datastore.datasource

import androidx.datastore.core.DataStore
import com.modules.core.datastore.models.PlayerPreferences
import timber.log.Timber
import javax.inject.Inject

/**
 * Data source for managing player preferences using DataStore
 */
class PlayerPreferencesDataSource @Inject constructor(
    private val playerPreferences: DataStore<PlayerPreferences>,
) : PreferencesDataSource<PlayerPreferences> {

    /**
     * Flow of player preferences
     */
    override val preferences = playerPreferences.data

    /**
     * Updates player preferences using transform function
     * @param transform Function to transform current preferences to new state
     */
    override suspend fun update(transform: suspend (PlayerPreferences) -> PlayerPreferences) {
        try {
            playerPreferences.updateData(transform)
        } catch (ioException: Exception) {
            Timber.tag("AppPlayerPreferences").e("Failed to update app preferences: $ioException")
        }
    }
}
