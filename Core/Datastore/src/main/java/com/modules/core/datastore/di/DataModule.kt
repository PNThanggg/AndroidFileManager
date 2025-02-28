package com.modules.core.datastore.di

import com.modules.core.datastore.repository.ILocalPreferencesRepository
import com.modules.core.datastore.repository.IPlayerPreferencesRepository
import com.modules.core.datastore.repository.LocalPreferencesRepository
import com.modules.core.datastore.repository.PlayerPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    fun bindsPreferencesRepository(
        preferencesRepository: LocalPreferencesRepository,
    ): ILocalPreferencesRepository

    @Binds
    fun bindsPlayerPreferencesRepository(
        playerPreferencesRepository: PlayerPreferencesRepository,
    ): IPlayerPreferencesRepository
}
