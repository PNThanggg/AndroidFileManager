package com.modules.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.module.core.di.ApplicationScope
import com.module.core.di.IODispatcher
import com.modules.core.datastore.models.ApplicationPreferences
import com.modules.core.datastore.models.PlayerPreferences
import com.modules.core.datastore.serializer.ApplicationPreferencesSerializer
import com.modules.core.datastore.serializer.PlayerPreferencesSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

private const val APP_PREFERENCES_DATASTORE_FILE = "app_preferences.json"
private const val PLAYER_PREFERENCES_DATASTORE_FILE = "player_preferences.json"

/**
 * Hilt module for DataStore preferences
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    /**
     * Provides DataStore for application preferences
     * @param context Application context
     * @param ioDispatcher IO dispatcher for background operations
     * @param scope Application coroutine scope
     * @return DataStore for ApplicationPreferences
     */
    @Provides
    @Singleton
    fun provideAppPreferencesDataStore(
        @ApplicationContext context: Context,
        @IODispatcher ioDispatcher: CoroutineDispatcher,
        @ApplicationScope scope: CoroutineScope,
    ): DataStore<ApplicationPreferences> {
        return DataStoreFactory.create(
            serializer = ApplicationPreferencesSerializer,
            scope = CoroutineScope(scope.coroutineContext + ioDispatcher),
            produceFile = { context.dataStoreFile(APP_PREFERENCES_DATASTORE_FILE) },
        )
    }

    /**
     * Provides DataStore for player preferences
     * @param applicationContext Application context
     * @param ioDispatcher IO dispatcher for background operations
     * @param scope Application coroutine scope
     * @return DataStore for PlayerPreferences
     */
    @Provides
    @Singleton
    fun providePlayerPreferencesDataStore(
        @ApplicationContext applicationContext: Context,
        @IODispatcher ioDispatcher: CoroutineDispatcher,
        @ApplicationScope scope: CoroutineScope,
    ): DataStore<PlayerPreferences> {
        return DataStoreFactory.create(
            serializer = PlayerPreferencesSerializer,
            scope = CoroutineScope(scope.coroutineContext + ioDispatcher),
            produceFile = { applicationContext.dataStoreFile(PLAYER_PREFERENCES_DATASTORE_FILE) },
        )
    }
}
