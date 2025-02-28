package com.modules.core.data.mappers

import com.module.core.extensions.formatDurationMillis
import com.module.core.extensions.formatFileSize
import com.modules.core.database.entities.AudioStreamInfoEntity
import com.modules.core.database.entities.SubtitleStreamInfoEntity
import com.modules.core.database.relations.MediumWithInfo
import com.modules.core.model.Video
import java.util.Date

fun MediumWithInfo.toVideo() = Video(
    id = mediumEntity.mediaStoreId,
    path = mediumEntity.path,
    parentPath = mediumEntity.parentPath,
    duration = mediumEntity.duration,
    uriString = mediumEntity.uriString,
    nameWithExtension = mediumEntity.name,
    width = mediumEntity.width,
    height = mediumEntity.height,
    size = mediumEntity.size,
    dateModified = mediumEntity.modified,
    format = mediumEntity.format,
    thumbnailPath = mediumEntity.thumbnailPath,
    playbackPosition = mediumEntity.playbackPosition,
    lastPlayedAt = mediumEntity.lastPlayedTime?.let { Date(it) },
    formattedDuration = mediumEntity.duration.formatDurationMillis,
    formattedFileSize = mediumEntity.size.formatFileSize,
    videoStream = videoStreamInfo?.toVideoStreamInfo(),
    audioStreams = audioStreamsInfo.map(AudioStreamInfoEntity::toAudioStreamInfo),
    subtitleStreams = subtitleStreamsInfo.map(SubtitleStreamInfoEntity::toSubtitleStreamInfo),
)
