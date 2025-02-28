package com.modules.core.data.mappers

import com.modules.core.database.entities.SubtitleStreamInfoEntity
import com.modules.core.model.SubtitleStreamInfo

fun SubtitleStreamInfoEntity.toSubtitleStreamInfo() = SubtitleStreamInfo(
    index = index,
    title = title,
    codecName = codecName,
    language = language,
    disposition = disposition,
)
