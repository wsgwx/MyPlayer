package dev.anilbeesetti.nextplayer.core.database

import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
class DaoModule {

    @Factory
    fun provideMediumStateDao(db: MediaDatabase) = db.mediumStateDao()

    @Factory
    fun provideHiddenVideoDao(db: MediaDatabase) = db.hiddenVideoDao()

    @Factory
    fun provideRemovedVideoDao(db: MediaDatabase) = db.removedVideoDao()
}
