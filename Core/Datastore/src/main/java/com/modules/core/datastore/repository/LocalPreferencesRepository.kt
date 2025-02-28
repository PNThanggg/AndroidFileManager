package com.modules.core.datastore.repository

import com.modules.core.datastore.datasource.AppPreferencesDataSource
import com.modules.core.datastore.models.ApplicationPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalPreferencesRepository @Inject constructor(
    private val appPreferencesDataSource: AppPreferencesDataSource,
) : ILocalPreferencesRepository {
    override val applicationPreferences: Flow<ApplicationPreferences>
        get() = appPreferencesDataSource.preferences

    override suspend fun updateApplicationPreferences(
        transform: suspend (ApplicationPreferences) -> ApplicationPreferences,
    ) {
        appPreferencesDataSource.update(transform)
    }
}
