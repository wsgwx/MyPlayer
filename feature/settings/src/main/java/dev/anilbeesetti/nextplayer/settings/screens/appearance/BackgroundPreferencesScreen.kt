package dev.anilbeesetti.nextplayer.settings.screens.appearance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anilbeesetti.nextplayer.core.model.BackgroundConfig
import dev.anilbeesetti.nextplayer.core.model.BackgroundType
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.ClickablePreferenceItem
import dev.anilbeesetti.nextplayer.core.ui.components.ListSectionTitle
import dev.anilbeesetti.nextplayer.core.ui.components.NextTopAppBar
import dev.anilbeesetti.nextplayer.core.ui.components.PreferenceItem
import dev.anilbeesetti.nextplayer.core.ui.components.rememberTvListFocusRequester
import dev.anilbeesetti.nextplayer.core.ui.components.tvFocusDown
import dev.anilbeesetti.nextplayer.core.ui.components.tvFocusRing
import dev.anilbeesetti.nextplayer.core.ui.components.tvListFocus
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons

@Composable
fun BackgroundPreferencesScreen(viewModel: BackgroundPreferencesViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BackgroundPreferencesScreenContent(state = state, onAction = viewModel::onAction)
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalLayoutApi::class,
)
@Composable
private fun BackgroundPreferencesScreenContent(
    state: BackgroundPreferencesUiState,
    onAction: (BackgroundPreferencesUiEvent) -> Unit,
) {
    val listFocusRequester = rememberTvListFocusRequester()
    val background = state.background

    Scaffold(
        topBar = {
            NextTopAppBar(
                title = stringResource(id = R.string.background_name),
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = { onAction(BackgroundPreferencesUiEvent.NavigateUp) },
                        modifier = Modifier.tvFocusDown(target = listFocusRequester),
                    ) {
                        Icon(
                            imageVector = NextIcons.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_up),
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .tvListFocus(focusRequester = listFocusRequester)
                .padding(paddingValues = innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            ListSectionTitle(text = stringResource(id = R.string.background_type))
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                BackgroundType.entries.forEachIndexed { index, type ->
                    PreferenceItem(
                        modifier = Modifier.tvFocusRing(shape = MaterialTheme.shapes.medium),
                        title = type.title(),
                        description = type.description(),
                        icon = type.icon(),
                        enabled = true,
                        onClick = { onAction(BackgroundPreferencesUiEvent.SelectType(type)) },
                        isFirstItem = index == 0,
                        isLastItem = index == BackgroundType.entries.lastIndex,
                        trailingContent = {
                            if (background.type == type) {
                                Icon(
                                    imageVector = NextIcons.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        },
                    )
                }
            }

            if (background.type == BackgroundType.IMAGE) {
                ListSectionTitle(text = stringResource(id = R.string.background_image))
                Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                    ClickablePreferenceItem(
                        modifier = Modifier.tvFocusRing(shape = MaterialTheme.shapes.medium),
                        title = stringResource(id = R.string.background_pick_image),
                        description = stringResource(id = R.string.background_pick_image_desc),
                        icon = NextIcons.Image,
                        onClick = { onAction(BackgroundPreferencesUiEvent.PickImage) },
                        isFirstItem = true,
                    )
                    ClickablePreferenceItem(
                        modifier = Modifier.tvFocusRing(shape = MaterialTheme.shapes.medium),
                        title = stringResource(id = R.string.background_clear_image),
                        icon = NextIcons.Delete,
                        onClick = { onAction(BackgroundPreferencesUiEvent.ClearImage) },
                        isLastItem = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun BackgroundType.title(): String = when (this) {
    BackgroundType.DEFAULT -> stringResource(id = R.string.background_default)
    BackgroundType.IMAGE -> stringResource(id = R.string.background_image_type)
}

@Composable
private fun BackgroundType.description(): String = when (this) {
    BackgroundType.DEFAULT -> stringResource(id = R.string.background_default_desc)
    BackgroundType.IMAGE -> stringResource(id = R.string.background_image_type_desc)
}

private fun BackgroundType.icon() = when (this) {
    BackgroundType.DEFAULT -> NextIcons.Appearance
    BackgroundType.IMAGE -> NextIcons.Image
}
