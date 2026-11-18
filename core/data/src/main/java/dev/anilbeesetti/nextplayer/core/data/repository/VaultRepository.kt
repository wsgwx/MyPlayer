package dev.anilbeesetti.nextplayer.core.data.repository

import dev.anilbeesetti.nextplayer.core.model.MediaInfo
import dev.anilbeesetti.nextplayer.core.model.Video
import kotlinx.coroutines.flow.Flow

interface VaultRepository {

    fun observeHiddenVideos(): Flow<List<Video>>

    /** Hides [videos] in place by renaming them. Returns how many were actually hidden. */
    suspend fun hideVideos(videos: List<Video>): Int

    /** Renames the given vault [videos] back to their original names. */
    suspend fun unhideVideos(videos: List<Video>): UnhideResult

    /** Permanently deletes the given vault [videos] (both the file and the record). */
    suspend fun deleteHiddenVideos(videos: List<Video>)

    suspend fun getHiddenVideoInfo(id: Long): MediaInfo?
}

/**
 * Outcome of an unhide operation.
 *
 * @param relocatedCount number of videos that couldn't be restored to their original folder
 *   (e.g. a custom folder or storage root, which scoped storage disallows) and were instead
 *   renamed back to their original names.
 */
data class UnhideResult(val requestedCount: Int = 0, val restoredCount: Int = 0)
