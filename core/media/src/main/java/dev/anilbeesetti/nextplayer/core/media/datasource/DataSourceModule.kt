package dev.anilbeesetti.nextplayer.core.media.datasource

import android.content.Context
import androidx.media3.datasource.DefaultDataSource
import org.koin.core.annotation.Single

@Single
fun provideDataSourceFactory(context: Context): DefaultDataSource.Factory =
    DefaultDataSource.Factory(context)
