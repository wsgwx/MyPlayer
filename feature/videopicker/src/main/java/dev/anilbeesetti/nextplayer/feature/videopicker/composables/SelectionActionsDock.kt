package dev.anilbeesetti.nextplayer.feature.videopicker.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.anilbeesetti.nextplayer.core.common.extensions.isTelevision
import dev.anilbeesetti.nextplayer.core.ui.components.thenIf
import dev.anilbeesetti.nextplayer.core.ui.components.tvFocusRing

/**
 * One action inside the selection dock.
 */
internal data class SelectionDockAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
    val isDestructive: Boolean = false,
    val enabled: Boolean = true,
)

/**
 * Contextual bottom dock shared by the library and the privacy space, so both selection menus look
 * and behave identically: it replaces the bottom region of the screen while items are selected.
 */
@Composable
internal fun SelectionActionsDock(
    modifier: Modifier = Modifier,
    show: Boolean,
    actions: List<SelectionDockAction>,
    contentFocusRequester: FocusRequester,
) {
    val context = LocalContext.current
    val isTv = remember { context.isTelevision }
    val firstActionFocusRequester = remember { FocusRequester() }

    AnimatedVisibility(
        modifier = modifier.windowInsetsPadding(
            WindowInsets.displayCutout.only(androidx.compose.foundation.layout.WindowInsetsSides.Horizontal),
        ),
        visible = show,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
    ) {
        val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = shape)
                .background(color = MaterialTheme.colorScheme.surfaceContainerHigh)
                .drawWithContent {
                    drawContent()
                    drawLine(
                        color = dividerColor,
                        start = Offset(x = 0f, y = 0f),
                        end = Offset(x = size.width, y = 0f),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
                .thenIf(condition = isTv) {
                    focusRestorer(fallback = firstActionFocusRequester)
                        .focusGroup()
                        .focusProperties { up = contentFocusRequester }
                }
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            actions.forEachIndexed { index, action ->
                DockActionItem(
                    modifier = Modifier.weight(weight = 1f).thenIf(condition = isTv && index == 0) {
                        focusRequester(focusRequester = firstActionFocusRequester)
                    },
                    action = action,
                    isTv = isTv,
                )
            }
        }
    }
}

@Composable
private fun DockActionItem(
    action: SelectionDockAction,
    isTv: Boolean,
    modifier: Modifier = Modifier,
) {
    val contentColor = when {
        !action.enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        action.isDestructive -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
    val containerColor = when {
        !action.enabled -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f)
        action.isDestructive -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    Column(
        modifier = modifier
            .tvFocusRing(
                isTv = isTv,
                shape = RoundedCornerShape(16.dp),
            )
            .clip(shape = RoundedCornerShape(16.dp))
            .clickable(enabled = action.enabled, onClick = action.onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(size = 40.dp)
                .background(color = containerColor, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.label,
                modifier = Modifier.size(size = 20.dp),
                tint = contentColor,
            )
        }
        Spacer(modifier = Modifier.height(height = 4.dp))
        Text(
            text = action.label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1,
        )
    }
}
