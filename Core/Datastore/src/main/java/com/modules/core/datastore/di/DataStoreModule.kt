package com.modules.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.modules.core.datastore.models.ApplicationPreferences
import com.modules.core.datastore.serializer.ApplicationPreferencesSerializer
import com.module.core.di.ApplicationScope
import com.module.core.di.IODispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

private const val APP_PREFERENCES_DATASTORE_FILE = "app_preferences.json"

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
}
