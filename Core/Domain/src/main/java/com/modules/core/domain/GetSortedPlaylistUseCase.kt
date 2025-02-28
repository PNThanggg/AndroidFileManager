package com.modules.core.domain

import android.content.Context
import android.net.Uri
import com.module.core.di.DefaultDispatcher
import com.module.core.extensions.getPath
import com.modules.core.datastore.repository.LocalPreferencesRepository
import com.modules.core.model.MediaViewMode
import com.modules.core.model.Video
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class GetSortedPlaylistUseCase @Inject constructor(
    private val getSortedVideosUseCase: GetSortedVideosUseCase,
    private val preferencesRepository: LocalPreferencesRepository,
    @ApplicationContext private val context: Context,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(uri: Uri): List<Video> = withContext(defaultDispatcher) {
        val path = context.getPath(uri) ?: return@withContext emptyList()
        val parent = File(path).parent.takeIf {
            preferencesRepository.applicationPreferences.first().mediaViewMode != MediaViewMode.VIDEOS
        }

        getSortedVideosUseCase.invoke(parent).first()
    }
}
