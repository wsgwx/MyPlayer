package dev.anilbeesetti.nextplayer.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A video the user removed from the library without deleting the file.
 *
 * Rows here only suppress the MediaStore entry from library listings; the underlying file keeps
 * living on the device and can still be played from history or opened externally.
 */
@Entity(tableName = "removed_video")
data class RemovedVideoEntity(
    @PrimaryKey
    @ColumnInfo(name = "uri")
    val uriString: String,
    @ColumnInfo(name = "path")
    val path: String,
    @ColumnInfo(name = "removed_at")
    val removedAt: Long = System.currentTimeMillis(),
)
