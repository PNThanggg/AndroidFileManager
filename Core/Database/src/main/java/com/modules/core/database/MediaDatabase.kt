package com.modules.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.modules.core.database.dao.DirectoryDao
import com.modules.core.database.dao.MediumDao
import com.modules.core.database.entities.AudioStreamInfoEntity
import com.modules.core.database.entities.DirectoryEntity
import com.modules.core.database.entities.MediumEntity
import com.modules.core.database.entities.SubtitleStreamInfoEntity
import com.modules.core.database.entities.VideoStreamInfoEntity

@Database(
    entities = [
        DirectoryEntity::class,
        MediumEntity::class,
        VideoStreamInfoEntity::class,
        AudioStreamInfoEntity::class,
        SubtitleStreamInfoEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class MediaDatabase : RoomDatabase() {

    abstract fun mediumDao(): MediumDao

    abstract fun directoryDao(): DirectoryDao

    companion object {
        const val DATABASE_NAME = "media_db"
    }
}
