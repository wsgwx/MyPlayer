package dev.anilbeesetti.nextplayer.core.common.service.system

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

interface SystemService {
    fun initialize(activity: ComponentActivity)
    suspend fun pickFolder(): Uri?

    /** Picks a single picture; the returned URI has a persisted read grant. */
    suspend fun pickImage(): Uri?
    fun getString(@StringRes stringResId: Int): String
    fun getQuantityString(
        @PluralsRes pluralsResId: Int,
        quantity: Int,
        vararg formatArgs: Any,
    ): String
    fun showToast(text: String, duration: Int)
}
