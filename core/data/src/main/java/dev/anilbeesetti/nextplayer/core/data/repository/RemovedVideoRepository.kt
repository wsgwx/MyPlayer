package dev.anilbeesetti.nextplayer.core.data.repository

import android.content.Context
import dev.anilbeesetti.nextplayer.core.common.extensions.VIDEO_COLLECTION_URI
import dev.anilbeesetti.nextplayer.core.database.dao.RemovedVideoDao
import dev.anilbeesetti.nextplayer.core.model.RemovedVideo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

interface RemovedVideoRepository {
    /** Videos hidden from the library with "remove record": the file still exists. */
    fun observeRemovedVideos(): Flow<List<RemovedVideo>>

    /** Puts the records back into the library (the files were never touched). */
    suspend fun restoreRemovedVideos(uris: List<String>)
}

@Factory
class LocalRemovedVideoRepository(
    private val removedVideoDao: RemovedVideoDao,
    private val context: Context,
) : RemovedVideoRepository {

    override fun observeRemovedVideos(): Flow<List<RemovedVideo>> = removedVideoDao.getAll().map { entities ->
        entities.map { entity ->
            RemovedVideo(
                uriString = entity.uriString,
                path = entity.path,
                removedAt = entity.removedAt,
            )
        }
    }

    override suspend fun restoreRemovedVideos(uris: List<String>) {
        if (uris.isEmpty()) return
        removedVideoDao.deleteByUris(uris = uris)
        // The library is a MediaStore query filtered by the ignore list; nudge its observer so the
        // restored videos reappear without waiting for some unrelated media change.
        context.contentResolver.notifyChange(VIDEO_COLLECTION_URI, null)
    }
}
