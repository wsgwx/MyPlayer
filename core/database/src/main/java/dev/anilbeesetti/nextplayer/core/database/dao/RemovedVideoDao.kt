package dev.anilbeesetti.nextplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.anilbeesetti.nextplayer.core.database.entities.RemovedVideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RemovedVideoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(videos: List<RemovedVideoEntity>)

    @Query("SELECT * FROM removed_video")
    fun getAll(): Flow<List<RemovedVideoEntity>>

    @Query("SELECT uri FROM removed_video")
    suspend fun getUris(): List<String>

    @Query("DELETE FROM removed_video WHERE uri IN (:uris)")
    suspend fun deleteByUris(uris: List<String>)

    @Query("DELETE FROM removed_video WHERE path NOT IN (:existingPaths)")
    suspend fun deleteMissingPaths(existingPaths: List<String>)
}
