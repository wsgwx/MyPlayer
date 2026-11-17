package dev.anilbeesetti.nextplayer.settings.screens.removed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.components.NextTopAppBar
import dev.anilbeesetti.nextplayer.core.ui.components.PreferenceItem
import dev.anilbeesetti.nextplayer.core.ui.components.rememberTvListFocusRequester
import dev.anilbeesetti.nextplayer.core.ui.components.tvFocusDown
import dev.anilbeesetti.nextplayer.core.ui.components.tvFocusRing
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import dev.anilbeesetti.nextplayer.core.ui.theme.NextPlayerTheme

@Composable
fun RemovedVideosScreen(viewModel: RemovedVideosViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RemovedVideosScreenContent(state = state, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RemovedVideosScreenContent(
    state: RemovedVideosUiState,
    onAction: (RemovedVideosUiEvent) -> Unit,
) {
    val listFocusRequester = rememberTvListFocusRequester()
    Scaffold(
        topBar = {
            NextTopAppBar(
                title = stringResource(id = R.string.removed_records),
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = { onAction(RemovedVideosUiEvent.NavigateUp) },
                        modifier = Modifier.tvFocusDown(target = listFocusRequester),
                    ) {
                        Icon(
                            imageVector = NextIcons.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_up),
                        )
                    }
                },
                actions = {
                    if (state.videos.isNotEmpty()) {
                        TextButton(onClick = { onAction(RemovedVideosUiEvent.RestoreAll) }) {
                            Text(text = stringResource(id = R.string.restore_all))
                        }
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        if (state.videos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues = innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(id = R.string.removed_records_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 8.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                itemsIndexed(state.videos, key = { _, item -> item.uriString }) { index, item ->
                    RemovedVideoItemRow(
                        item = item,
                        isFirst = index == 0,
                        isLast = index == state.videos.lastIndex,
                        onRestore = { onAction(RemovedVideosUiEvent.Restore(item.uriString)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RemovedVideoItemRow(
    item: RemovedVideoItem,
    isFirst: Boolean,
    isLast: Boolean,
    onRestore: () -> Unit,
) {
    PreferenceItem(
        modifier = Modifier
            .fillMaxWidth()
            .tvFocusRing(shape = MaterialTheme.shapes.medium),
        title = item.name,
        description = item.path,
        icon = NextIcons.Video,
        enabled = true,
        isFirstItem = isFirst,
        isLastItem = isLast,
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onRestore) {
                    Text(
                        text = stringResource(id = R.string.restore_record),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun RemovedVideosScreenPreview() {
    NextPlayerTheme {
        RemovedVideosScreenContent(
            state = RemovedVideosUiState(
                videos = listOf(
                    RemovedVideoItem(
                        uriString = "content://media/1",
                        name = "Movie.mp4",
                        path = "/storage/emulated/0/Movies/Movie.mp4",
                    ),
                ),
            ),
            onAction = {},
        )
    }
}
