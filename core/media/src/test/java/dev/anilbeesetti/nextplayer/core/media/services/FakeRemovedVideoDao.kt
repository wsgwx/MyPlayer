package dev.anilbeesetti.nextplayer.core.media.services

import dev.anilbeesetti.nextplayer.core.database.dao.RemovedVideoDao
import dev.anilbeesetti.nextplayer.core.database.entities.RemovedVideoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRemovedVideoDao : RemovedVideoDao {
    private val videos = MutableStateFlow<List<RemovedVideoEntity>>(emptyList())

    val removed: List<RemovedVideoEntity> get() = videos.value

    override suspend fun insertAll(videos: List<RemovedVideoEntity>) {
        val byUri = this.videos.value.associateBy { it.uriString }.toMutableMap()
        videos.forEach { byUri[it.uriString] = it }
        this.videos.value = byUri.values.toList()
    }

    override fun getAll(): Flow<List<RemovedVideoEntity>> = videos

    override suspend fun getUris(): List<String> = videos.value.map { it.uriString }

    override suspend fun deleteByUris(uris: List<String>) {
        videos.value = videos.value.filterNot { it.uriString in uris }
    }

    override suspend fun deleteMissingPaths(existingPaths: List<String>) {
        videos.value = videos.value.filter { it.path in existingPaths }
    }
}
