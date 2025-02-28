package com.modules.core.domain


import com.module.core.di.IODispatcher
import com.modules.core.data.repository.MediaRepository
import com.modules.core.datastore.repository.LocalPreferencesRepository
import com.modules.core.model.Folder
import com.modules.core.model.Sort
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetSortedFoldersUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val preferencesRepository: LocalPreferencesRepository,
    @IODispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<List<Folder>> {
        return combine(
            mediaRepository.getFoldersFlow(),
            preferencesRepository.applicationPreferences,
        ) { folders, preferences ->

            val nonExcludedDirectories = folders.filter {
                it.mediaList.isNotEmpty() && it.path !in preferences.excludeFolders
            }

            val sort = Sort(by = preferences.sortBy, order = preferences.sortOrder)
            nonExcludedDirectories.sortedWith(sort.folderComparator())
        }.flowOn(defaultDispatcher)
    }
}
