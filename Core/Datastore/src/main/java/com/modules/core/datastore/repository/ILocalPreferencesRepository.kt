package com.modules.core.datastore.repository

import com.modules.core.datastore.models.ApplicationPreferences
import kotlinx.coroutines.flow.Flow

interface ILocalPreferencesRepository {

    /**
     * Stream of [ApplicationPreferences].
     */
    val applicationPreferences: Flow<ApplicationPreferences>


    suspend fun updateApplicationPreferences(
        transform: suspend (ApplicationPreferences) -> ApplicationPreferences,
    )
}
