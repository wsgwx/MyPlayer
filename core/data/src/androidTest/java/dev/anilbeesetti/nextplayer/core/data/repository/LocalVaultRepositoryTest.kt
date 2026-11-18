package dev.anilbeesetti.nextplayer.core.data.repository

import android.content.Context
import androidx.core.net.toUri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.anilbeesetti.nextplayer.core.database.MediaDatabase
import dev.anilbeesetti.nextplayer.core.model.Video
import java.io.File
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The vault hides videos in place by renaming them within their folder; unhiding renames them
 * back. Files never move between folders.
 */
@RunWith(AndroidJUnit4::class)
class LocalVaultRepositoryTest {
    private lateinit var database: MediaDatabase
    private lateinit var context: Context
    private lateinit var repository: LocalVaultRepository
    private lateinit var folder: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            context,
            MediaDatabase::class.java,
        ).build()
        repository = LocalVaultRepository(
            hiddenVideoDao = database.hiddenVideoDao(),
            mediumStateDao = database.mediumStateDao(),
            context = context,
        )
        folder = File(context.cacheDir, "vault-test-${UUID.randomUUID()}").apply { mkdirs() }
    }

    @After
    fun tearDown() {
        folder.deleteRecursively()
        database.close()
    }

    private fun newVideo(name: String, bytes: Int = 16): File {
        val file = File(folder, name)
        file.writeBytes(ByteArray(bytes))
        return file
    }

    private fun videoFor(file: File) = Video.sample.copy(
        path = file.absolutePath,
        parentPath = file.parent.orEmpty(),
        uriString = file.toUri().toString(),
        nameWithExtension = file.name,
    )

    @Test
    fun hideRenamesFileInPlaceWithHiddenDotName() = runBlocking {
        val file = newVideo("movie.mp4")
        repository.hideVideos(videos = listOf(videoFor(file)))

        val hidden = database.hiddenVideoDao().getAll().first().single()
        val renamed = File(hidden.vaultPath)
        // Same folder, file still exists, but under a dot-prefixed name without extension.
        assertEquals(folder.absolutePath, renamed.parent)
        assertTrue(renamed.exists())
        assertFalse(renamed.name.startsWith("movie"))
        assertTrue(renamed.name.startsWith("."))
        assertFalse(renamed.extension.isNotEmpty())
        assertEquals(file.absolutePath, hidden.originalPath)
        assertFalse(file.exists())
    }

    @Test
    fun unhideRenamesBackToOriginalName() = runBlocking {
        val file = newVideo("movie.mp4")
        repository.hideVideos(videos = listOf(videoFor(file)))
        val entity = database.hiddenVideoDao().getAll().first().single()
        val renamed = File(entity.vaultPath)

        repository.unhideVideos(
            videos = listOf(
                Video.sample.copy(
                    id = entity.id,
                    uriString = renamed.toUri().toString(),
                    nameWithExtension = entity.displayName,
                ),
            ),
        )

        assertTrue(file.exists())
        assertFalse(renamed.exists())
        assertTrue(database.hiddenVideoDao().getAll().first().isEmpty())
    }

    @Test
    fun unhideKeepsRecordWhenOriginalNameIsTaken() = runBlocking {
        val file = newVideo("movie.mp4")
        repository.hideVideos(videos = listOf(videoFor(file)))
        val entity = database.hiddenVideoDao().getAll().first().single()
        // Someone else creates a file at the original path before the unhide.
        File(entity.originalPath).writeBytes(ByteArray(8))

        val result = repository.unhideVideos(
            videos = listOf(
                Video.sample.copy(
                    id = entity.id,
                    uriString = File(entity.vaultPath).toUri().toString(),
                    nameWithExtension = entity.displayName,
                ),
            ),
        )

        assertEquals(0, result.relocatedCount)
        // The renamed file must remain recorded, not orphaned.
        assertTrue(database.hiddenVideoDao().getAll().first().isNotEmpty())
        assertTrue(File(entity.vaultPath).exists())
    }

    @Test
    fun deleteRemovesRenamedFileAndRecord() = runBlocking {
        val file = newVideo("movie.mp4")
        repository.hideVideos(videos = listOf(videoFor(file)))
        val entity = database.hiddenVideoDao().getAll().first().single()

        repository.deleteHiddenVideos(
            videos = listOf(
                Video.sample.copy(
                    id = entity.id,
                    uriString = File(entity.vaultPath).toUri().toString(),
                    nameWithExtension = entity.displayName,
                ),
            ),
        )

        assertFalse(File(entity.vaultPath).exists())
        assertTrue(database.hiddenVideoDao().getAll().first().isEmpty())
    }

    @Test
    fun hideSkipsFilesThatNoLongerExist() = runBlocking {
        val missing = File(folder, "gone.mp4")
        repository.hideVideos(videos = listOf(videoFor(missing)))

        assertTrue(database.hiddenVideoDao().getAll().first().isEmpty())
        assertFalse(missing.exists())
    }

    @Test
    fun hideTwiceDoesNotDuplicateRecords() = runBlocking {
        val file = newVideo("movie.mp4")
        val video = videoFor(file)
        repository.hideVideos(videos = listOf(video))
        val afterFirst = database.hiddenVideoDao().getAll().first().single()

        // The second attempt targets the original path, which no longer exists.
        repository.hideVideos(videos = listOf(video))

        val records = database.hiddenVideoDao().getAll().first()
        assertEquals(1, records.size)
        assertEquals(afterFirst.vaultPath, records.single().vaultPath)
        assertNotNull(File(records.single().vaultPath))
    }
}
