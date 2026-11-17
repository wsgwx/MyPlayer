package dev.anilbeesetti.nextplayer.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationPreferences(
    val sortBy: Sort.By = Sort.By.TITLE,
    val sortOrder: Sort.Order = Sort.Order.ASCENDING,
    val themeConfig: ThemeConfig = ThemeConfig.SYSTEM,
    /** App-wide colour scheme. */
    val themePalette: ThemePalette = ThemePalette.CINEMA,
    val useHighContrastDarkTheme: Boolean = false,
    val useDynamicColors: Boolean = true,
    val markLastPlayedMedia: Boolean = true,
    /** When non-empty, only videos living under one of these paths are shown. */
    val scanFolders: List<String> = emptyList(),
    val mediaViewMode: MediaViewMode = MediaViewMode.FOLDERS,
    val mediaLayoutMode: MediaLayoutMode = MediaLayoutMode.LIST,

    /** Background of the library and the privacy space. */
    val background: BackgroundConfig = BackgroundConfig(),

    // Network browser
    val networkSortBy: Sort.By = Sort.By.TITLE,
    val networkSortOrder: Sort.Order = Sort.Order.ASCENDING,
    val networkMediaLayoutMode: MediaLayoutMode = MediaLayoutMode.LIST,

    // Fields
    val showDurationField: Boolean = true,
    val showFolderDurationField: Boolean = true,
    val showExtensionField: Boolean = false,
    val showPathField: Boolean = true,
    val showResolutionField: Boolean = false,
    val showSizeField: Boolean = false,
    val showThumbnailField: Boolean = true,
    val showPlayedProgress: Boolean = true,

    val newVideoThresholdDays: Int = 7,

    // Thumbnail generation
    val thumbnailGenerationStrategy: ThumbnailGenerationStrategy =
        ThumbnailGenerationStrategy.FRAME_AT_PERCENTAGE,
    val thumbnailFramePosition: Float = DEFAULT_THUMBNAIL_FRAME_POSITION,
) {

    companion object {
        const val DEFAULT_THUMBNAIL_FRAME_POSITION = 0.33f
    }
}

/**
 * True when [path] may be shown: either no scan folder is configured (scan everything) or the path
 * lives inside one of the configured scan folders.
 */
fun ApplicationPreferences.isUnderScanFolder(path: String?): Boolean {
    if (scanFolders.isEmpty()) return true
    if (path.isNullOrBlank()) return false
    return scanFolders.any { folder -> path == folder || path.startsWith(folder.trimEnd('/') + "/") }
}

/**
 * Folder counterpart of [isUnderScanFolder]: keeps a folder if it lives inside a scan folder or is
 * one of its ancestors, so the user can still browse down into a configured scan folder.
 */
fun ApplicationPreferences.isRelevantFolder(path: String?): Boolean {
    if (scanFolders.isEmpty()) return true
    if (path.isNullOrBlank()) return false
    return scanFolders.any { folder ->
        path == folder ||
            path.startsWith(folder.trimEnd('/') + "/") ||
            folder.startsWith(path.trimEnd('/') + "/")
    }
}
