package com.modules.core.domain


import com.module.core.di.DefaultDispatcher
import com.modules.core.data.repository.MediaRepository
import com.modules.core.datastore.repository.LocalPreferencesRepository
import com.modules.core.model.Sort
import com.modules.core.model.Video
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetSortedVideosUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val preferencesRepository: LocalPreferencesRepository,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    operator fun invoke(folderPath: String? = null): Flow<List<Video>> {
        val videosFlow = if (folderPath != null) {
            mediaRepository.getVideosFlowFromFolderPath(folderPath)
        } else {
            mediaRepository.getVideosFlow()
        }

        return combine(
            videosFlow,
            preferencesRepository.applicationPreferences,
        ) { videoItems, preferences ->

            val nonExcludedVideos = videoItems.filterNot {
                it.parentPath in preferences.excludeFolders
            }

            val sort = Sort(by = preferences.sortBy, order = preferences.sortOrder)
            nonExcludedVideos.sortedWith(sort.videoComparator())
        }.flowOn(defaultDispatcher)
    }
}
