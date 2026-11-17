package dev.anilbeesetti.nextplayer.feature.videopicker.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.anilbeesetti.nextplayer.core.model.Video
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.CancelButton
import dev.anilbeesetti.nextplayer.core.ui.components.DoneButton
import dev.anilbeesetti.nextplayer.core.ui.components.NextDialog
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons

private const val MAX_RATING = 10

/**
 * Rate a video from 1 to 10 and describe it in your own words. Deliberately free of the
 * technical media info — this screen is only about what you think of the film.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingDialog(
    video: Video,
    onDismiss: () -> Unit,
    onSave: (rating: Float?, comment: String) -> Unit,
) {
    var rating by remember { mutableStateOf(value = video.userRating) }
    var comment by remember { mutableStateOf(value = video.userComment) }

    NextDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = video.displayName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        content = {
            Column(
                modifier = Modifier.verticalScroll(state = rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // ---- Score: 1..10 round chips ----
                Text(
                    text = stringResource(id = R.string.rating_score),
                    style = MaterialTheme.typography.titleSmall,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(times = MAX_RATING) { index ->
                        val value = index + 1
                        val selected = rating?.toInt() == value
                        ScoreChip(
                            value = value,
                            selected = selected,
                            onClick = {
                                rating = if (selected) null else value.toFloat()
                            },
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = NextIcons.Review,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(size = 18.dp),
                    )
                    Text(
                        text = rating?.let { "${it.toInt()} / $MAX_RATING" }
                            ?: stringResource(id = R.string.rating_unrated),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // ---- Comment ----
                Text(
                    text = stringResource(id = R.string.rating_comment),
                    style = MaterialTheme.typography.titleSmall,
                )
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    placeholder = {
                        Text(text = stringResource(id = R.string.rating_comment_hint))
                    },
                )
            }
        },
        confirmButton = {
            DoneButton(
                onClick = {
                    onSave(
                        rating,
                        comment.trim(),
                    )
                },
            )
        },
        dismissButton = { CancelButton(onClick = onDismiss) },
    )
}

@Composable
private fun ScoreChip(value: Int, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = value.toString(),
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier
            .size(size = 30.dp)
            .clip(shape = CircleShape)
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
            )
            .clickable(onClick = onClick),
        textAlign = TextAlign.Center,
    )
}
