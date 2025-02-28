package com.modules.core.data.mappers

import com.module.core.extensions.formatFileSize
import com.modules.core.database.relations.DirectoryWithMedia
import com.modules.core.database.relations.MediumWithInfo
import com.modules.core.model.Folder

fun DirectoryWithMedia.toFolder() = Folder(
    name = directory.name,
    path = directory.path,
    dateModified = directory.modified,
    parentPath = directory.parentPath,
    formattedMediaSize = media.sumOf { it.mediumEntity.size }.formatFileSize,
    mediaList = media.map(MediumWithInfo::toVideo),
)
