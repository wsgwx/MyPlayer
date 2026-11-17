package dev.anilbeesetti.nextplayer.feature.videopicker.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.anilbeesetti.nextplayer.core.model.ApplicationPreferences
import dev.anilbeesetti.nextplayer.core.model.MediaLayoutMode
import dev.anilbeesetti.nextplayer.core.model.Video
import dev.anilbeesetti.nextplayer.core.model.isNew
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.NextSegmentedListItem
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import dev.anilbeesetti.nextplayer.core.ui.theme.NextPlayerTheme
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

@Composable
fun VideoItem(
    video: Video,
    isRecentlyPlayedVideo: Boolean,
    preferences: ApplicationPreferences,
    modifier: Modifier = Modifier,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
) {
    when (preferences.mediaLayoutMode) {
        MediaLayoutMode.LIST -> VideoListItem(
            video = video,
            isRecentlyPlayedVideo = isRecentlyPlayedVideo,
            preferences = preferences,
            modifier = modifier,
            isFirstItem = isFirstItem,
            isLastItem = isLastItem,
            selected = selected,
            onClick = onClick,
            onLongClick = onLongClick,
        )

        MediaLayoutMode.GRID -> VideoGridItem(
            video = video,
            isRecentlyPlayedVideo = isRecentlyPlayedVideo,
            preferences = preferences,
            modifier = modifier,
            isFirstItem = isFirstItem,
            isLastItem = isLastItem,
            selected = selected,
            onClick = onClick,
            onLongClick = onLongClick,
        )
    }
}

@OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
fun VideoListItem(
    video: Video,
    isRecentlyPlayedVideo: Boolean,
    preferences: ApplicationPreferences,
    modifier: Modifier = Modifier,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
) {
    val path = video.path.substringBeforeLast(delimiter = "/")
    val showPath = preferences.showPathField && path.isNotBlank()
    val showSize =
        preferences.showSizeField && video.size > 0 && video.formattedFileSize.isNotBlank()
    val showResolution = preferences.showResolutionField && video.height > 0

    NextSegmentedListItem(
        modifier = modifier,
        selected = selected,
        contentPadding = PaddingValues(8.dp),
        colors = ListItemDefaults.segmentedColors(
            contentColor = if (isRecentlyPlayedVideo && preferences.markLastPlayedMedia) {
                MaterialTheme.colorScheme.primary
            } else {
                ListItemDefaults.segmentedColors().contentColor
            },
            supportingContentColor = if (isRecentlyPlayedVideo && preferences.markLastPlayedMedia) {
                MaterialTheme.colorScheme.primary
            } else {
                ListItemDefaults.colors().supportingContentColor
            },
            selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
        ),
        isFirstItem = isFirstItem,
        isLastItem = isLastItem,
        onClick = onClick,
        onLongClick = onLongClick,
        leadingContent = {
            ThumbnailView(
                video = video,
                preferences = preferences,
                modifier = Modifier
                    .width(
                        width = min(
                            150.dp,
                            b =
                                LocalConfiguration.current.screenWidthDp.dp * 0.35f,
                        ),
                    ),
            )
        },
        content = {
            Text(
                text = if (preferences.showExtensionField) {
                    video.nameWithExtension
                } else {
                    video.displayName
                },
                maxLines = 2,
                style = MaterialTheme.typography.titleMedium.copy(lineBreak = LineBreak.Heading),
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = if (showPath || showSize || showResolution) {
            {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (showPath) {
                        Text(
                            text = path,
                            maxLines = 2,
                            style = MaterialTheme.typography.bodySmall,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (showSize || showResolution) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            if (showSize) {
                                InfoChip(text = video.formattedFileSize)
                            }
                            if (showResolution) {
                                InfoChip(text = "${video.height}p")
                            }
                        }
                    }
                }
            }
        } else {
            null
        },
    )
}

/**
 * Infuse-style poster card: rounded artwork with the title underneath, no list chrome.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VideoGridItem(
    modifier: Modifier = Modifier,
    video: Video,
    isRecentlyPlayedVideo: Boolean,
    preferences: ApplicationPreferences,
    textStyle: TextStyle = MaterialTheme.typography.titleSmall,
    @Suppress("UNUSED_PARAMETER") isFirstItem: Boolean = false,
    @Suppress("UNUSED_PARAMETER") isLastItem: Boolean = false,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
) {
    val isHighlighted = isRecentlyPlayedVideo && preferences.markLastPlayedMedia
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                },
                shape = shape,
            )
            .then(
                if (selected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = shape,
                    )
                } else {
                    Modifier
                },
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(all = 6.dp),
        verticalArrangement = Arrangement.spacedBy(space = 6.dp),
    ) {
        ThumbnailView(
            modifier = Modifier.fillMaxWidth(),
            video = video,
            preferences = preferences,
        )
        Text(
            text = if (preferences.showExtensionField) {
                video.nameWithExtension
            } else {
                video.displayName
            },
            maxLines = 2,
            style = textStyle,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            color = if (isHighlighted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(
                horizontal = 2.dp,
                vertical = 1.dp,
            ),
        )
    }
}

/** True if the video is local. Remote videos avoid full download and use a placeholder. */
private fun Video.isLocalUri(): Boolean {
    val scheme = uriString.toUri().scheme
    return scheme.equals(
        other = "content",
        ignoreCase = true,
    ) ||
        scheme.equals(
            other = "file",
            ignoreCase = true,
        )
}

@Composable
private fun ThumbnailView(
    modifier: Modifier = Modifier,
    video: Video,
    preferences: ApplicationPreferences,
) {
    val context = LocalContext.current
    val isLocalVideo = remember(video.uriString) { video.isLocalUri() }
    val isNew by produceState(
        initialValue = video.isNew(thresholdDays = preferences.newVideoThresholdDays),
        key1 = video.dateAdded,
        key2 = video.lastPlayedAt,
        key3 = preferences.newVideoThresholdDays,
    ) {
        while (value) {
            delay(60.seconds)
            value = video.isNew(thresholdDays = preferences.newVideoThresholdDays)
        }
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            .aspectRatio(ratio = 16f / 10f),
    ) {
        Icon(
            imageVector = NextIcons.Video,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surfaceColorAtElevation(100.dp),
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(fraction = 0.5f),
        )
        if (preferences.showThumbnailField && isLocalVideo) {
            AsyncImage(
                model = ImageRequest.Builder(context = context)
                    .data(video.uriString)
                    .crossfade(enable = true)
                    .build(),
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (preferences.showDurationField && video.duration > 0 &&
            video.formattedDuration.isNotBlank()
        ) {
            InfoChip(
                text = video.formattedDuration,
                modifier = Modifier
                    .padding(5.dp)
                    .align(Alignment.BottomEnd),
                backgroundColor = Color.Black.copy(alpha = 0.6f),
                contentColor = Color.White,
                shape = MaterialTheme.shapes.extraSmall,
            )
        }

        if (isNew) {
            InfoChip(
                text = stringResource(R.string.new_label),
                modifier = Modifier
                    .padding(5.dp)
                    .align(Alignment.TopStart),
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.extraSmall,
            )
        }

        video.userRating?.let { rating ->
            RatingBadge(
                rating = rating,
                modifier = Modifier
                    .padding(all = 5.dp)
                    .align(alignment = Alignment.TopEnd),
            )
        }

        if (preferences.showPlayedProgress && video.playedPercentage != null) {
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = video.playedPercentage!!)
                        .fillMaxHeight()
                        .clip(shape = CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun VideoItemRecentlyPlayedPreview() {
    NextPlayerTheme {
        Surface {
            VideoListItem(
                video = Video.sample,
                preferences = ApplicationPreferences(),
                isRecentlyPlayedVideo = true,
            )
        }
    }
}

@PreviewLightDark
@Composable
fun VideoItemPreview() {
    NextPlayerTheme {
        Surface {
            VideoListItem(
                video = Video.sample,
                preferences = ApplicationPreferences(),
                isRecentlyPlayedVideo = false,
            )
        }
    }
}

@PreviewLightDark
@Composable
fun VideoGridItemPreview() {
    NextPlayerTheme {
        VideoGridItem(
            video = Video.sample,
            preferences = ApplicationPreferences(),
            isRecentlyPlayedVideo = true,
        )
    }
}

/** Small score badge pinned to the thumbnail's top-right corner. */
@Composable
fun RatingBadge(rating: Float, modifier: Modifier = Modifier) {
    Text(
        text = if (rating % 1f == 0f) {
            rating.toInt().toString()
        } else {
            String.format(
                java.util.Locale.US,
                "%.1f",
                rating,
            )
        },
        style = MaterialTheme.typography.labelMedium,
        color = Color(0xFFFFB74D),
        modifier = modifier
            .clip(shape = CircleShape)
            .background(color = Color.Black.copy(alpha = 0.65f))
            .padding(
                horizontal = 7.dp,
                vertical = 2.dp,
            ),
    )
}
