package dev.anilbeesetti.nextplayer.core.model

/** A video removed from the library via "remove record": the file itself still exists. */
data class RemovedVideo(val uriString: String, val path: String, val removedAt: Long)
