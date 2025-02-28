package com.modules.core.data.mappers

import com.modules.core.data.models.VideoState
import com.modules.core.database.converter.UriListConverter
import com.modules.core.database.entities.MediumEntity

fun MediumEntity.toVideoState(): VideoState {
    return VideoState(
        path = path,
        title = name,
        position = playbackPosition.takeIf { it != 0L },
        audioTrackIndex = audioTrackIndex,
        subtitleTrackIndex = subtitleTrackIndex,
        playbackSpeed = playbackSpeed,
        externalSubs = UriListConverter.fromStringToList(externalSubs),
        videoScale = videoScale,
        thumbnailPath = thumbnailPath,
    )
}
