package dev.anilbeesetti.nextplayer.feature.videopicker.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.anilbeesetti.nextplayer.core.model.ApplicationPreferences
import dev.anilbeesetti.nextplayer.core.model.MediaLayoutMode
import dev.anilbeesetti.nextplayer.core.model.MediaViewMode
import dev.anilbeesetti.nextplayer.core.model.Sort
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.CancelButton
import dev.anilbeesetti.nextplayer.core.ui.components.DoneButton
import dev.anilbeesetti.nextplayer.core.ui.components.NextDialog
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import dev.anilbeesetti.nextplayer.feature.videopicker.extensions.name

/**
 * Trimmed quick settings: only how the library is browsed and how the items are laid out.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSettingsDialog(
    applicationPreferences: ApplicationPreferences,
    onDismiss: () -> Unit,
    updatePreferences: (ApplicationPreferences) -> Unit,
) {
    var preferences by remember { mutableStateOf(value = applicationPreferences) }

    NextDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.quick_settings)) },
        content = {
            HorizontalDivider()
            Column(modifier = Modifier.verticalScroll(state = rememberScrollState())) {
                DialogSectionTitle(text = stringResource(R.string.media_view_mode))
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    MediaViewMode.entries.forEachIndexed { index, viewMode ->
                        SegmentedButton(
                            selected = preferences.mediaViewMode == viewMode,
                            onClick = { preferences = preferences.copy(mediaViewMode = viewMode) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = MediaViewMode.entries.size,
                            ),
                            colors = SegmentedButtonDefaults.colors(
                                activeContentColor = MaterialTheme.colorScheme.primary,
                                activeBorderColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) { Text(text = viewMode.name()) }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
                DialogSectionTitle(text = stringResource(R.string.media_layout))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    MediaLayoutMode.entries.forEachIndexed { index, layoutMode ->
                        SegmentedButton(
                            selected = preferences.mediaLayoutMode == layoutMode,
                            onClick = {
                                preferences = preferences.copy(mediaLayoutMode = layoutMode)
                            },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = MediaLayoutMode.entries.size,
                            ),
                            colors = SegmentedButtonDefaults.colors(
                                activeContentColor = MaterialTheme.colorScheme.primary,
                                activeBorderColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Text(
                                text = stringResource(
                                    id = if (layoutMode == MediaLayoutMode.LIST) {
                                        R.string.list
                                    } else {
                                        R.string.grid
                                    },
                                ),
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
                DialogSectionTitle(text = stringResource(R.string.sort_by))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Sort.By.entries.forEach { by ->
                        FilterChip(
                            selected = preferences.sortBy == by,
                            onClick = { preferences = preferences.copy(sortBy = by) },
                            label = { Text(text = by.name()) },
                        )
                    }
                }
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    Sort.Order.entries.forEachIndexed { index, order ->
                        SegmentedButton(
                            selected = preferences.sortOrder == order,
                            onClick = {
                                preferences = preferences.copy(sortOrder = order)
                            },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = Sort.Order.entries.size,
                            ),
                            colors = SegmentedButtonDefaults.colors(
                                activeContentColor = MaterialTheme.colorScheme.primary,
                                activeBorderColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) { Text(text = order.name(sortBy = preferences.sortBy)) }
                    }
                }
            }
        },
        confirmButton = {
            DoneButton(
                onClick = {
                    updatePreferences(preferences)
                    onDismiss()
                },
            )
        },
        dismissButton = { CancelButton(onClick = onDismiss) },
    )
}

@Composable
fun FieldChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedIcon: ImageVector = NextIcons.CheckBox,
    unselectedIcon: ImageVector = NextIcons.CheckBoxOutline,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label) },
        leadingIcon = {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = "",
                modifier = Modifier.size(FilterChipDefaults.IconSize),
                tint = MaterialTheme.colorScheme.secondary,
            )
        },
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderWidth = 1.dp,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = modifier,
    )
}

@Composable
private fun DialogSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(
            top = 16.dp,
            bottom = 8.dp,
        ),
    )
}

@Preview
@Composable
fun QuickSettingsPreview() {
    Surface {
        QuickSettingsDialog(
            applicationPreferences = ApplicationPreferences(),
            onDismiss = {},
            updatePreferences = {},
        )
    }
}
