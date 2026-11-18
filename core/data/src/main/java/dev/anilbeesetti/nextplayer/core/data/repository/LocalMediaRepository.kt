package dev.anilbeesetti.nextplayer.core.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import dev.anilbeesetti.nextplayer.core.common.Utils
import dev.anilbeesetti.nextplayer.core.common.extensions.mapAsync
import dev.anilbeesetti.nextplayer.core.data.mappers.toAudioStreamInfo
import dev.anilbeesetti.nextplayer.core.data.mappers.toFolder
import dev.anilbeesetti.nextplayer.core.data.mappers.toSubtitleStreamInfo
import dev.anilbeesetti.nextplayer.core.data.mappers.toVideo
import dev.anilbeesetti.nextplayer.core.data.mappers.toVideoState
import dev.anilbeesetti.nextplayer.core.data.mappers.toVideoStreamInfo
import dev.anilbeesetti.nextplayer.core.data.models.VideoState
import dev.anilbeesetti.nextplayer.core.database.converter.UriListConverter
import dev.anilbeesetti.nextplayer.core.database.dao.HiddenVideoDao
import dev.anilbeesetti.nextplayer.core.database.dao.MediumStateDao
import dev.anilbeesetti.nextplayer.core.database.entities.MediumStateEntity
import dev.anilbeesetti.nextplayer.core.datastore.datasource.AppPreferencesDataSource
import dev.anilbeesetti.nextplayer.core.media.services.MediaService
import dev.anilbeesetti.nextplayer.core.model.Folder
import dev.anilbeesetti.nextplayer.core.model.MediaInfo
import dev.anilbeesetti.nextplayer.core.model.Video
import io.github.anilbeesetti.nextlib.mediainfo.MediaInfoBuilder
import java.util.Date
import kotlin.math.absoluteValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory

@Factory
class LocalMediaRepository(
    private val mediumStateDao: MediumStateDao,
    private val hiddenVideoDao: HiddenVideoDao,
    private val mediaService: MediaService,
    private val appPreferencesDataSource: AppPreferencesDataSource,
    private val context: Context,
) : MediaRepository {

    override fun observeFolders(folderPath: String?): Flow<List<Folder>> = combine(
        mediaService.observeFolders(folderPath = folderPath),
        hiddenVideoDao.getAll(),
    ) { mediaFolders, hiddenVideos ->
        // The vault hides videos in place, so MediaStore still counts them; subtract.
        val hiddenByFolder = hiddenVideos
            .map { it.vaultPath }
            .groupBy { it.substringBeforeLast('/', missingDelimiterValue = "") }
            .mapValues { it.value.size }
        mediaFolders.map { folder ->
            folder.toFolder().let { folderInfo ->
                folderInfo.copy(
                    videosCount = (folderInfo.videosCount - (hiddenByFolder[folder.path] ?: 0))
                        .coerceAtLeast(0),
                )
            }
        }
    }

    override fun observeVideos(folderPath: String?): Flow<List<Video>> = combine(
        mediaService.observeVideos(folderPath = folderPath),
        mediumStateDao.getAll(),
        hiddenVideoDao.getAll(),
    ) {
            mediaVideos,
            mediumStates,
            hiddenVideos,
        ->
        // Cover both the renamed file and the original name: MediaStore may still carry the
        // original record until its next scan catches up with the rename.
        val hiddenPaths = hiddenVideos.fold(
            initial = mutableSetOf<String>(),
        ) { acc, entity ->
            acc.add(entity.vaultPath)
            acc.add(entity.originalPath)
            acc
        }
        val statesMap = mediumStates.associateBy { it.uriString }
        mediaVideos
            .map { mediaVideo ->
                val mediaState = statesMap[mediaVideo.uri.toString()]
                mediaVideo.toVideo(mediaState = mediaState)
            }
            // The vault hides videos in place: drop records hidden in the vault.
            .filterNot { it.path in hiddenPaths }
    }

    override suspend fun fetchFolders(folderPath: String?): List<Folder> =
        mediaService.fetchFolders(folderPath = folderPath).map { it.toFolder() }

    override suspend fun fetchVideos(folderPath: String?): List<Video> =
        mediaService.fetchVideos(folderPath = folderPath).mapAsync { mediaVideo ->
            val mediaState = mediumStateDao.get(mediaVideo.uri.toString())
            mediaVideo.toVideo(mediaState = mediaState)
        }

    override suspend fun getVideoByUri(uri: String): Video? = coroutineScope {
        val mediaVideoDeferred = async { mediaService.findVideo(uri.toUri()) }
        val mediaStateDeferred = async { mediumStateDao.get(uri = uri) }

        val mediaVideo = mediaVideoDeferred.await() ?: return@coroutineScope null
        val mediaState = mediaStateDeferred.await()

        return@coroutineScope mediaVideo.toVideo(mediaState = mediaState)
    }

    override suspend fun getVideoState(uri: String): VideoState? =
        mediumStateDao.get(uri = uri)?.toVideoState()

    override suspend fun getMediaInfo(uri: String): MediaInfo? = withContext(Dispatchers.IO) {
        val video = getVideoByUri(uri = uri) ?: return@withContext null
        val mediaInfo = runCatching {
            MediaInfoBuilder().from(
                context = context,
                uri = uri.toUri(),
            ).build()
        }.getOrNull()
        val result = MediaInfo(
            video = video.copy(format = mediaInfo?.format),
            videoStream = mediaInfo?.videoStream?.toVideoStreamInfo(),
            audioStreams = mediaInfo?.audioStreams?.map { it.toAudioStreamInfo() } ?: emptyList(),
            subtitleStreams =
                mediaInfo?.subtitleStreams?.map { it.toSubtitleStreamInfo() } ?: emptyList(),
        )
        mediaInfo?.release()
        return@withContext result
    }

    override suspend fun clearPlaybackHistory() {
        mediumStateDao.clearPlaybackHistory()
    }

    override suspend fun updateMediumLastPlayedTime(
        uri: String,
        lastPlayedTime: Long,
        duration: Long?,
    ) {
        // Vault playback must never surface as "last played" in the library.
        val recordLastPlayed = !uri.toUri().isVaultUri()
        mediumStateDao.update(uri = uri) { state ->
            state.copy(
                lastPlayedTime = if (recordLastPlayed) lastPlayedTime else state.lastPlayedTime,
                duration = duration ?: state.duration,
            )
        }
    }

    override suspend fun updateMediumPosition(uri: String, position: Long) {
        mediumStateDao.update(uri = uri) { it.copy(playbackPosition = position) }
    }

    override suspend fun updateMediumAudioTrack(uri: String, audioTrackIndex: Int) {
        mediumStateDao.update(uri = uri) { it.copy(audioTrackIndex = audioTrackIndex) }
    }

    override suspend fun updateMediumSubtitleTrack(uri: String, subtitleTrackIndex: Int) {
        mediumStateDao.update(uri = uri) { it.copy(subtitleTrackIndex = subtitleTrackIndex) }
    }

    override suspend fun updateMediumZoom(uri: String, zoom: Float) {
        mediumStateDao.update(uri = uri) { it.copy(videoScale = zoom) }
    }

    override suspend fun updateMediumUserReview(uri: String, rating: Float?, comment: String?) {
        mediumStateDao.update(uri = uri) { state ->
            state.copy(
                userRating = rating,
                userComment = comment ?: state.userComment,
            )
        }
    }

    override suspend fun addExternalAudioToMedium(uri: String, audioUri: Uri) {
        val stateEntity = mediumStateDao.get(uri = uri) ?: MediumStateEntity(uriString = uri)
        val audio = UriListConverter.fromStringToList(stateEntity.externalAudio)
        if (audioUri in audio) return
        mediumStateDao.upsert(
            stateEntity.copy(
                externalAudio = UriListConverter.fromListToString(
                    urlList =
                        audio + audioUri,
                ),
            ),
        )
    }

    override suspend fun addExternalSubtitleToMedium(uri: String, subtitleUri: Uri) {
        mediumStateDao.update(uri = uri) { state ->
            val subtitles = UriListConverter.fromStringToList(state.externalSubs)
            state.copy(
                externalSubs = UriListConverter.fromListToString(
                    (subtitles + subtitleUri).distinct(),
                ),
            )
        }
    }

    override suspend fun updateSubtitleDelay(uri: String, delay: Long) {
        mediumStateDao.update(uri = uri) { it.copy(subtitleDelayMilliseconds = delay) }
    }

    override suspend fun updateSubtitleSpeed(uri: String, speed: Float) {
        mediumStateDao.update(uri = uri) { it.copy(subtitleSpeed = speed) }
    }

    private fun Uri.isVaultUri(): Boolean {
        if (scheme != "content" || authority != "${context.packageName}.fileprovider") return false
        val segments = pathSegments
        val root = segments.firstOrNull()
        return root == "vault" ||
            (
                segments.getOrNull(index = 1) == "vault" &&
                    (root == "external_files_path" || root == "files_path")
                )
    }
}

private fun MediumStateEntity.toHistoryVideo(): Video {
    val uri = uriString.toUri()
    val name = uri.lastPathSegment?.substringAfterLast(delimiter = '/')
        ?: uri.host
        ?: uriString
    return Video(
        id = uriString.hashCode().toLong().absoluteValue,
        path = uriString,
        uriString = uriString,
        nameWithExtension = name,
        duration = duration ?: 0,
        width = 0,
        height = 0,
        size = 0,
        formattedDuration = duration?.let(block = Utils::formatDurationMillis).orEmpty(),
        playbackPosition = playbackPosition,
        lastPlayedAt = lastPlayedTime?.let(block = ::Date),
    )
}
