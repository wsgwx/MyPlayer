package dev.anilbeesetti.nextplayer.core.data.repository

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import dev.anilbeesetti.nextplayer.core.common.Logger
import dev.anilbeesetti.nextplayer.core.common.Utils
import dev.anilbeesetti.nextplayer.core.common.extensions.getPath
import dev.anilbeesetti.nextplayer.core.data.mappers.toAudioStreamInfo
import dev.anilbeesetti.nextplayer.core.data.mappers.toSubtitleStreamInfo
import dev.anilbeesetti.nextplayer.core.data.mappers.toVideo
import dev.anilbeesetti.nextplayer.core.data.mappers.toVideoStreamInfo
import dev.anilbeesetti.nextplayer.core.database.dao.HiddenVideoDao
import dev.anilbeesetti.nextplayer.core.database.dao.MediumStateDao
import dev.anilbeesetti.nextplayer.core.database.entities.HiddenVideoEntity
import dev.anilbeesetti.nextplayer.core.model.MediaInfo
import dev.anilbeesetti.nextplayer.core.model.Video
import io.github.anilbeesetti.nextlib.mediainfo.MediaInfoBuilder
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

/**
 * The vault hides videos **in place** by renaming them: the file never leaves its folder, it
 * just gets a random dot-prefixed name with no extension, so it still lives at the original
 * path while galleries and other players no longer recognize it as a video. Unhiding renames
 * it back; permanent deletion removes the renamed file and the record.
 *
 * Renaming requires all-files access on API 30+ (see the hide flow in the media picker).
 */
@Single
class LocalVaultRepository(
    private val hiddenVideoDao: HiddenVideoDao,
    private val mediumStateDao: MediumStateDao,
    private val context: Context,
) : VaultRepository {

    private val vaultMutationMutex = Mutex()

    override fun observeHiddenVideos(): Flow<List<Video>> =
        hiddenVideoDao.getAll().map { entities ->
            entities
                .filter { File(it.vaultPath).exists() }
                .map { it.toVideo() }
        }

    override suspend fun hideVideos(videos: List<Video>): Int = vaultMutationMutex.withLock {
        val hiddenAt = System.currentTimeMillis()
        var hiddenCount = 0
        videos.forEach { video ->
            val source = withContext(context = Dispatchers.IO) {
                context.getPath(uri = video.uriString.toUri())?.let(::File)
                    ?: video.path.let(::File).takeIf { it.exists() }
            } ?: return@forEach

            // Same-directory rename: O(1) regardless of file size.
            val destination = createHiddenName(source)
            val renamed = withContext(context = Dispatchers.IO) {
                runCatching { source.renameTo(destination) }
                    .onFailure {
                        Logger.logError(
                            tag = TAG,
                            message = "hide: rename failed for $source: $it",
                        )
                    }
                    .getOrDefault(false)
            }
            if (!renamed) return@forEach

            runCatching {
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(source.path, destination.path),
                    null,
                    null,
                )
            }

            hiddenVideoDao.insert(
                HiddenVideoEntity(
                    vaultPath = destination.absolutePath,
                    originalPath = source.absolutePath,
                    displayName = video.nameWithExtension,
                    duration = video.duration,
                    size = video.size,
                    width = video.width,
                    height = video.height,
                    hiddenAt = hiddenAt,
                ),
            )
            // Hidden videos must not surface as "last played" anywhere.
            mediumStateDao.clearLastPlayedTimes(listOf(video.uriString))
            hiddenCount++
        }
        return hiddenCount
    }

    override suspend fun unhideVideos(videos: List<Video>): UnhideResult =
        vaultMutationMutex.withLock {
            var restored = 0
            val entities = entitiesFor(videos = videos)
            entities.forEach { entity ->
                val vaultFile = File(entity.vaultPath)
                if (!vaultFile.exists()) {
                    hiddenVideoDao.deleteByIds(listOf(entity.id))
                    return@forEach
                }
                val originalFile = File(entity.originalPath)
                val renamedBack = withContext(context = Dispatchers.IO) {
                    runCatching {
                        originalFile.parentFile?.mkdirs()
                        vaultFile.renameTo(originalFile)
                    }.getOrDefault(false)
                }
                if (renamedBack) {
                    restored++
                    runCatching {
                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf(originalFile.path, entity.vaultPath),
                            null,
                            null,
                        )
                    }
                    hiddenVideoDao.deleteByIds(listOf(entity.id))
                } else {
                    // Keep the record: the renamed file still exists and must stay recoverable.
                    Logger.logError(
                        tag = TAG,
                        message = "unhide: rename back failed for ${entity.vaultPath}",
                    )
                }
            }
            UnhideResult(
                requestedCount = entities.size,
                restoredCount = restored,
            )
        }

    override suspend fun deleteHiddenVideos(videos: List<Video>): Unit =
        vaultMutationMutex.withLock {
            val entities = entitiesFor(videos = videos)
            if (entities.isEmpty()) return@withLock
            val deletedIds = mutableListOf<Long>()
            entities.forEach { entity ->
                val deleted = withContext(context = Dispatchers.IO) {
                    runCatching { File(entity.vaultPath).delete() }
                        .onFailure {
                            logCleanupFailure(
                                operation = "delete file ${entity.vaultPath}",
                                throwable = it,
                            )
                        }
                        .getOrDefault(false)
                }
                if (deleted) {
                    deletedIds += entity.id
                } else {
                    // Keep the record so the file stays visible and deletable in the vault.
                }
            }
            hiddenVideoDao.deleteByIds(deletedIds)
            // The files were indexed by MediaStore under their original names; rescan so the
            // library reflects the removal immediately.
            runCatching {
                MediaScannerConnection.scanFile(
                    context,
                    entities.map { it.vaultPath }.toTypedArray(),
                    null,
                    null,
                )
            }
        }

    override suspend fun getHiddenVideoInfo(id: Long): MediaInfo? {
        val entity = hiddenVideoDao.getById(id = id) ?: return null
        val video = entity.toVideo()
        val mediaInfo = runCatching {
            MediaInfoBuilder().from(
                context = context,
                uri = Uri.fromFile(File(entity.vaultPath)),
            ).build()
        }.getOrNull()

        return MediaInfo(
            video = video.copy(format = mediaInfo?.format),
            videoStream = mediaInfo?.videoStream?.toVideoStreamInfo(),
            audioStreams = mediaInfo?.audioStreams?.map { it.toAudioStreamInfo() } ?: emptyList(),
            subtitleStreams =
                mediaInfo?.subtitleStreams?.map { it.toSubtitleStreamInfo() } ?: emptyList(),
        ).also { mediaInfo?.release() }
    }

    /**
     * A hidden name in the same folder: dot-prefixed (skipped by MediaProvider and hidden in
     * file managers) with no extension, so nothing recognizes it as a video anymore.
     */
    private fun createHiddenName(source: File): File = generateSequence {
        File(source.parentFile, "." + UUID.randomUUID().toString().replace("-", "").take(16))
    }
        .first { !it.exists() }

    /** Looks up the vault entities backing the given [videos] by their row id. */
    private suspend fun entitiesFor(videos: List<Video>): List<HiddenVideoEntity> {
        val ids = videos.map { it.id }.toSet()
        return hiddenVideoDao.getAll().first().filter { it.id in ids }
    }

    private fun logCleanupFailure(operation: String, throwable: Throwable) {
        Logger.logError(
            tag = TAG,
            message = "Failed to $operation: ${throwable.stackTraceToString()}",
        )
    }

    private fun HiddenVideoEntity.toVideo(): Video {
        val uriString = fileProviderUri(file = File(vaultPath)).toString()
        return Video(
            id = id,
            path = vaultPath,
            parentPath = File(vaultPath).parent ?: "",
            duration = duration,
            uriString = uriString,
            nameWithExtension = displayName,
            width = width,
            height = height,
            size = size,
            dateModified = hiddenAt,
            dateAdded = hiddenAt / 1000L,
            formattedDuration = Utils.formatDurationMillis(millis = duration),
            formattedFileSize = Utils.formatFileSize(size = size),
        )
    }

    private fun fileProviderUri(file: File): Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )

    companion object {
        private const val TAG = "LocalVaultRepository"
    }
}
