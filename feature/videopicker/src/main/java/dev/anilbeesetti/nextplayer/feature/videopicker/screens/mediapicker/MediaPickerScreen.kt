package dev.anilbeesetti.nextplayer.feature.videopicker.screens.mediapicker

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import dev.anilbeesetti.nextplayer.core.common.extensions.isTelevision
import dev.anilbeesetti.nextplayer.core.common.storagePermission
import dev.anilbeesetti.nextplayer.core.domain.MediaHolder
import dev.anilbeesetti.nextplayer.core.media.services.TransferMode
import dev.anilbeesetti.nextplayer.core.media.services.TransferProgress
import dev.anilbeesetti.nextplayer.core.model.ApplicationPreferences
import dev.anilbeesetti.nextplayer.core.model.Folder
import dev.anilbeesetti.nextplayer.core.model.MediaLayoutMode
import dev.anilbeesetti.nextplayer.core.model.MediaViewMode
import dev.anilbeesetti.nextplayer.core.model.Video
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.base.DataState
import dev.anilbeesetti.nextplayer.core.ui.components.BindTopLevelBottomBarVisible
import dev.anilbeesetti.nextplayer.core.ui.components.BindTopLevelFab
import dev.anilbeesetti.nextplayer.core.ui.components.CancelButton
import dev.anilbeesetti.nextplayer.core.ui.components.LocalNavigationBottomPadding
import dev.anilbeesetti.nextplayer.core.ui.components.NextDialog
import dev.anilbeesetti.nextplayer.core.ui.components.NextOutlinedTextField
import dev.anilbeesetti.nextplayer.core.ui.components.NextTopAppBar
import dev.anilbeesetti.nextplayer.core.ui.components.horizontalSwipe
import dev.anilbeesetti.nextplayer.core.ui.components.TextIconToggleButton
import dev.anilbeesetti.nextplayer.core.ui.components.TopLevelFabKey
import dev.anilbeesetti.nextplayer.core.ui.components.rememberRestorableFocusState
import dev.anilbeesetti.nextplayer.core.ui.components.thenIf
import dev.anilbeesetti.nextplayer.core.ui.components.tvFocusRing
import dev.anilbeesetti.nextplayer.core.ui.composables.PermissionMissingView
import dev.anilbeesetti.nextplayer.core.ui.designsystem.NextIcons
import dev.anilbeesetti.nextplayer.core.ui.extensions.copy
import dev.anilbeesetti.nextplayer.core.ui.preview.DayNightPreview
import dev.anilbeesetti.nextplayer.core.ui.preview.VideoPickerPreviewParameterProvider
import dev.anilbeesetti.nextplayer.core.ui.theme.NextPlayerTheme
import dev.anilbeesetti.nextplayer.feature.videopicker.composables.CenterCircularProgressBar
import dev.anilbeesetti.nextplayer.feature.videopicker.composables.MediaInfoDialog
import dev.anilbeesetti.nextplayer.feature.videopicker.composables.MediaView
import dev.anilbeesetti.nextplayer.feature.videopicker.composables.QuickSettingsDialog
import dev.anilbeesetti.nextplayer.feature.videopicker.composables.NoVideosFound
import dev.anilbeesetti.nextplayer.feature.videopicker.composables.RatingDialog
import dev.anilbeesetti.nextplayer.feature.videopicker.composables.RenameDialog
import dev.anilbeesetti.nextplayer.feature.videopicker.composables.SelectionActionsDock
import dev.anilbeesetti.nextplayer.feature.videopicker.composables.SelectionDockAction
import dev.anilbeesetti.nextplayer.feature.videopicker.composables.vault.PinDotsIndicator
import dev.anilbeesetti.nextplayer.feature.videopicker.composables.vault.PinKeypad
import dev.anilbeesetti.nextplayer.feature.videopicker.composables.vault.VaultBiometricSetupDialog
import dev.anilbeesetti.nextplayer.feature.videopicker.composables.vault.VaultProgressDialog
import dev.anilbeesetti.nextplayer.feature.videopicker.screens.vault.VAULT_PIN_LENGTH
import dev.anilbeesetti.nextplayer.feature.videopicker.state.SelectionItem
import dev.anilbeesetti.nextplayer.feature.videopicker.state.rememberSelectionManager
import dev.anilbeesetti.nextplayer.feature.videopicker.state.toSelectedFolder
import dev.anilbeesetti.nextplayer.feature.videopicker.state.toSelectedVideo
import kotlin.math.roundToInt

@Composable
fun MediaPickerScreen(viewModel: MediaPickerViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle(
        minActiveState = Lifecycle.State.RESUMED,
    )

    MediaPickerScreenContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalPermissionsApi::class,
)
@Composable
internal fun MediaPickerScreenContent(
    state: MediaPickerUiState,
    onAction: (MediaPickerAction) -> Unit = {},
) {
    val selectionManager = rememberSelectionManager()
    val context = LocalContext.current
    val isTv = remember { context.isTelevision }
    val focusState = rememberRestorableFocusState()
    val hasMedia = (state.mediaDataState as? DataState.Success)?.value
        ?.let { it.folders.isNotEmpty() || it.videos.isNotEmpty() } == true
    val navigationBottomPadding = LocalNavigationBottomPadding.current

    // Re-enter the content region so its previously focused item is restored.
    val topBarDownModifier = if (isTv && hasMedia) {
        Modifier.focusProperties { down = focusState.requester }
    } else {
        Modifier
    }
    val permissionState = rememberPermissionState(permission = storagePermission)
    var wasPermissionGranted by remember { mutableStateOf(permissionState.status.isGranted) }

    LaunchedEffect(permissionState.status.isGranted) {
        val isPermissionGranted = permissionState.status.isGranted
        if (isPermissionGranted && !wasPermissionGranted) {
            onAction(MediaPickerAction.OnPermissionAccepted)
        }
        wasPermissionGranted = isPermissionGranted
    }
    val lazyGridState = rememberLazyGridState()
    val selectVideoFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> uri?.let { onAction(MediaPickerAction.OnPlayVideo(uri = it)) } },
    )

    var showRenameActionFor: Video? by rememberSaveable { mutableStateOf(value = null) }
    var showRatingActionFor: Video? by rememberSaveable { mutableStateOf(value = null) }
    var showQuickSettings by rememberSaveable { mutableStateOf(value = false) }
    var showDeleteVideosConfirmation by rememberSaveable { mutableStateOf(value = false) }

    val mediaHolder = (state.mediaDataState as? DataState.Success)?.value
    val onFabClick = {
        val selectedItem = state.recentlyPlayedVideo?.toSelectedVideo()
            ?: mediaHolder?.folders?.firstOrNull()?.toSelectedFolder()
            ?: mediaHolder?.videos?.firstOrNull()?.toSelectedVideo()

        when (selectedItem) {
            is SelectionItem.Video -> onAction(
                MediaPickerAction.OnPlayVideo(selectedItem.uriString.toUri()),
            )

            is SelectionItem.Folder -> onAction(
                MediaPickerAction.PlaySelectedItems(selectionItems = setOf(element = selectedItem)),
            )

            null -> selectVideoFileLauncher.launch(input = "video/*")
        }
    }

    BindTopLevelBottomBarVisible(
        visible =
            state.folderName != null || !selectionManager.isInSelectionMode,
    )

    if (state.folderName == null) {
        BindTopLevelFab(
            key = TopLevelFabKey.MEDIA,
            icon = NextIcons.Play,
            onClick = onFabClick,
        )
    }

    val selectedItemsSize = selectionManager.selectionItems.size
    val totalItemsSize =
        (state.mediaDataState as? DataState.Success)?.value?.run { folders.size + videos.size } ?: 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Pager-like gesture: swiping left reveals the privacy space, which slides in from the
            // right edge, so the direction matches where the screen comes from.
            .horizontalSwipe(
                enabled = !selectionManager.isInSelectionMode,
                onSwipeLeft = { onAction(MediaPickerAction.OpenVault) },
            ),
    ) {
        Scaffold(
        topBar = {
            NextTopAppBar(
                title = {
                    val titleText = (state.folderName ?: stringResource(R.string.app_name))
                        .takeIf { !selectionManager.isInSelectionMode } ?: ""
                    Text(
                        text = titleText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold.takeIf { state.folderName == null },
                    )
                },
                navigationIcon = {
                    if (selectionManager.isInSelectionMode) {
                        Row(
                            modifier = Modifier
                                .clip(shape = CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .clickable { selectionManager.exitSelectionMode() }
                                .padding(8.dp)
                                .padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = NextIcons.Close,
                                contentDescription = stringResource(id = R.string.navigate_up),
                            )
                            Text(
                                text = stringResource(
                                    R.string.m_n_selected,
                                    selectedItemsSize,
                                    totalItemsSize,
                                ),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    } else if (state.folderName != null) {
                        FilledTonalIconButton(
                            onClick = { onAction(MediaPickerAction.OnNavigateUpClick) },
                            modifier = topBarDownModifier.tvFocusRing(isTv = isTv),
                        ) {
                            Icon(
                                imageVector = NextIcons.ArrowBack,
                                contentDescription = stringResource(id = R.string.navigate_up),
                            )
                        }
                    }
                },
                actions = {
                    if (selectionManager.isInSelectionMode) {
                        FilledTonalIconButton(
                            onClick = {
                                if (selectedItemsSize != totalItemsSize) {
                                    val folder = (state.mediaDataState as? DataState.Success)?.value
                                    if (folder != null) {
                                        folder.folders.forEach {
                                            selectionManager.selectFolder(folder = it)
                                        }
                                        folder.videos.forEach {
                                            selectionManager.selectVideo(video = it)
                                        }
                                    }
                                } else {
                                    selectionManager.clearSelection()
                                }
                            },
                            modifier = topBarDownModifier.tvFocusRing(isTv = isTv),
                        ) {
                            Icon(
                                imageVector = if (selectedItemsSize != totalItemsSize) {
                                    NextIcons.SelectAll
                                } else {
                                    NextIcons.DeselectAll
                                },
                                contentDescription = if (selectedItemsSize != totalItemsSize) {
                                    stringResource(R.string.select_all)
                                } else {
                                    stringResource(R.string.deselect_all)
                                },
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { onAction(MediaPickerAction.OpenAppearance) },
                            modifier = topBarDownModifier.tvFocusRing(isTv = isTv),
                        ) {
                            Icon(
                                imageVector = NextIcons.Appearance,
                                contentDescription = stringResource(R.string.appearance_name),
                            )
                        }
                        val layoutMode = state.preferences.mediaLayoutMode
                        IconButton(
                            onClick = {
                                onAction(
                                    MediaPickerAction.UpdateMenu(
                                        preferences = state.preferences.copy(
                                            mediaLayoutMode = if (
                                                layoutMode == MediaLayoutMode.LIST
                                            ) {
                                                MediaLayoutMode.GRID
                                            } else {
                                                MediaLayoutMode.LIST
                                            },
                                        ),
                                    ),
                                )
                            },
                            modifier = topBarDownModifier
                                .tvFocusRing(isTv = isTv)
                                .combinedClickable(
                                    onClick = {
                                        onAction(
                                            MediaPickerAction.UpdateMenu(
                                                preferences = state.preferences.copy(
                                                    mediaLayoutMode = if (
                                                        layoutMode == MediaLayoutMode.LIST
                                                    ) {
                                                        MediaLayoutMode.GRID
                                                    } else {
                                                        MediaLayoutMode.LIST
                                                    },
                                                ),
                                            ),
                                        )
                                    },
                                    onLongClick = { showQuickSettings = true },
                                ),
                        ) {
                            Icon(
                                imageVector = if (layoutMode == MediaLayoutMode.LIST) {
                                    NextIcons.DashBoard
                                } else {
                                    NextIcons.Sensitivity
                                },
                                contentDescription = stringResource(
                                    id = if (layoutMode == MediaLayoutMode.LIST) {
                                        R.string.grid
                                    } else {
                                        R.string.list
                                    },
                                ),
                            )
                        }
                        IconButton(
                            onClick = { onAction(MediaPickerAction.OnSettingsClick) },
                            modifier = topBarDownModifier.tvFocusRing(isTv = isTv),
                        ) {
                            Icon(
                                imageVector = NextIcons.Settings,
                                contentDescription = stringResource(id = R.string.settings),
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            SelectionActionsSheet(
                show =
                    selectionManager.isInSelectionMode &&
                        selectionManager.selectionItems.isNotEmpty(),
                contentFocusRequester = focusState.requester,
                showRenameAction = selectionManager.isSingleVideoSelected,
                showInfoAction = selectionManager.isSingleVideoSelected,
                showRateAction = selectionManager.isSingleVideoSelected,
                onPlayAction = {
                    onAction(MediaPickerAction.PlaySelectedItems(selectionManager.selectionItems))
                    selectionManager.exitSelectionMode()
                },
                onHideAction = {
                    onAction(MediaPickerAction.HideSelectedItems(selectionManager.selectionItems))
                    selectionManager.exitSelectionMode()
                },
                onRenameAction = {
                    val selectedVideo =
                        selectionManager.selectionItems.firstOrNull()
                            ?: return@SelectionActionsSheet
                    val video = (state.mediaDataState as? DataState.Success)?.value?.videos
                        ?.find { it.uriString == selectedVideo.id } ?: return@SelectionActionsSheet
                    showRenameActionFor = video
                },
                onInfoAction = {
                    val selectedVideo =
                        selectionManager.selectionItems.firstOrNull()
                            ?: return@SelectionActionsSheet
                    val video = (state.mediaDataState as? DataState.Success)?.value?.videos
                        ?.find { it.uriString == selectedVideo.id } ?: return@SelectionActionsSheet
                    onAction(MediaPickerAction.ShowMediaInfo(video = video))
                    selectionManager.exitSelectionMode()
                },
                onRateAction = {
                    val selectedVideo =
                        selectionManager.selectionItems.firstOrNull()
                            ?: return@SelectionActionsSheet
                    val video = (state.mediaDataState as? DataState.Success)?.value?.videos
                        ?.find { it.uriString == selectedVideo.id } ?: return@SelectionActionsSheet
                    showRatingActionFor = video
                    selectionManager.exitSelectionMode()
                },
                onDeleteAction = { showDeleteVideosConfirmation = true },
            )
        },
        floatingActionButton = {
            if (state.folderName != null && !selectionManager.isInSelectionMode) {
                FloatingActionButton(
                    onClick = onFabClick,
                    modifier = Modifier
                        .tvFocusRing(shape = MaterialTheme.shapes.large)
                        .focusProperties { if (isTv && hasMedia) up = focusState.requester },
                    shape = MaterialTheme.shapes.large,
                ) {
                    Icon(
                        imageVector = NextIcons.Play,
                        contentDescription = stringResource(R.string.play),
                    )
                }
            }
        },
        containerColor = Color.Transparent,
    ) { scaffoldPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(scaffoldPadding.copy(bottom = 0.dp))
                .clip(
                    shape = RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp,
                    ),
                )
                .background(MaterialTheme.colorScheme.background),
        ) {
            if (!permissionState.status.isGranted) {
                PermissionMissingView(
                    isGranted = permissionState.status.isGranted,
                    showRationale = permissionState.status.shouldShowRationale,
                    permission = permissionState.permission,
                    launchPermissionRequest = { permissionState.launchPermissionRequest() },
                ) {}
                return@Scaffold
            }
            when (state.mediaDataState) {
                is DataState.Error -> {
                }

                is DataState.Loading -> {
                    CenterCircularProgressBar()
                }

                is DataState.Success -> {
                    val successContent: @Composable () -> Unit = {
                        val updatedScaffoldPadding = scaffoldPadding.copy(
                            top = 0.dp,
                            start = 0.dp,
                            bottom =
                                scaffoldPadding.calculateBottomPadding() + navigationBottomPadding,
                        )
                        val mediaHolder = state.mediaDataState.value
                        if (mediaHolder == null ||
                            (mediaHolder.folders.isEmpty() && mediaHolder.videos.isEmpty())
                        ) {
                            NoVideosFound(contentPadding = updatedScaffoldPadding)
                        } else {
                            MediaView(
                                recentlyPlayedVideo = state.recentlyPlayedVideo,
                                recentlyPlayedFolder = state.recentlyPlayedFolder,
                                mediaHolder = mediaHolder,
                                preferences = state.preferences,
                                onFolderClick = {
                                    onAction(MediaPickerAction.OnFolderClick(folderPath = it))
                                },
                                onVideoClick = {
                                    onAction(MediaPickerAction.OnPlayVideo(uri = it))
                                },
                                selectionManager = selectionManager,
                                lazyGridState = lazyGridState,
                                focusState = focusState,
                                contentPadding = updatedScaffoldPadding,
                            )
                        }
                    }
                    if (isTv) {
                        successContent()
                    } else {
                        PullToRefreshBox(
                            isRefreshing = state.refreshing,
                            onRefresh = { onAction(MediaPickerAction.Refresh) },
                        ) { successContent() }
                    }
                }
            }
        }
    }

    BackHandler(enabled = selectionManager.isInSelectionMode) {
        selectionManager.exitSelectionMode()
    }

    showRenameActionFor?.let { video ->
        RenameDialog(
            name = video.displayName,
            onDismiss = { showRenameActionFor = null },
            onDone = {
                onAction(
                    MediaPickerAction.RenameVideo(
                        video.uriString.toUri(),
                        to = it,
                    ),
                )
                selectionManager.exitSelectionMode()
                showRenameActionFor = null
            },
        )
    }

    if (showQuickSettings) {
        QuickSettingsDialog(
            applicationPreferences = state.preferences,
            onDismiss = { showQuickSettings = false },
            updatePreferences = { preferences ->
                onAction(MediaPickerAction.UpdateMenu(preferences = preferences))
            },
        )
    }

    showRatingActionFor?.let { video ->
        RatingDialog(
            video = video,
            onDismiss = { showRatingActionFor = null },
            onSave = { rating, comment ->
                onAction(
                    MediaPickerAction.RateVideo(
                        uri = video.uriString,
                        rating = rating,
                        comment = comment,
                    ),
                )
                showRatingActionFor = null
            },
        )
    }

    state.mediaInfo?.let { mediaInfo ->
        MediaInfoDialog(
            mediaInfo = mediaInfo,
            onDismiss = { onAction(MediaPickerAction.DismissMediaInfo) },
        )
    }

    if (showDeleteVideosConfirmation) {
        DeleteConfirmationDialog(
            selectionItems = selectionManager.selectionItems,
            onRemoveRecord = {
                onAction(
                    MediaPickerAction.RemoveSelectedItemsFromLibrary(
                        selectionManager.selectionItems,
                    ),
                )
                selectionManager.exitSelectionMode()
                showDeleteVideosConfirmation = false
            },
            onDeletePermanently = {
                onAction(
                    MediaPickerAction.DeleteSelectedItems(selectionManager.selectionItems),
                )
                selectionManager.exitSelectionMode()
                showDeleteVideosConfirmation = false
            },
            onCancel = { showDeleteVideosConfirmation = false },
        )
    }

    (state.transferFlow as? TransferFlowState.Processing)?.let { transfer ->
        TransferProgressDialog(
            mode = transfer.mode,
            progress = transfer.progress,
            onCancel = { onAction(MediaPickerAction.CancelTransfer) },
        )
    }
    }
}

@Composable
private fun TransferProgressDialog(
    mode: TransferMode,
    progress: TransferProgress,
    onCancel: () -> Unit,
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(dismissOnClickOutside = false),
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp,
                        vertical = 24.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = when (mode) {
                        TransferMode.COPY -> stringResource(R.string.copying_videos_in_progress)
                        TransferMode.MOVE -> stringResource(R.string.moving_videos_in_progress)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                ProgressSection(
                    label = progress.currentName.orEmpty(),
                    fraction = progress.currentFraction,
                )

                if (progress.totalFiles > 1) {
                    ProgressSection(
                        label = stringResource(
                            R.string.transfer_file_progress,
                            progress.currentIndex + 1,
                            progress.totalFiles,
                        ),
                        fraction = progress.overallFraction,
                    )
                }

                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.align(Alignment.End),
                ) { Text(text = stringResource(R.string.cancel)) }
            }
        }
    }
}

/**
 * A labelled progress bar. A null [fraction] renders an indeterminate bar with no percentage.
 */
@Composable
private fun ProgressSection(label: String, fraction: Float?) {
    val animatedFraction by animateFloatAsState(
        targetValue = fraction ?: 0f,
        label = "progress",
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(weight = 1f),
            )
            if (fraction != null) {
                Text(
                    text = "${(fraction * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        val barModifier = Modifier
            .fillMaxWidth()
            .clip(shape = CircleShape)
        if (fraction != null) {
            LinearProgressIndicator(
                progress = { animatedFraction },
                modifier = barModifier,
            )
        } else {
            LinearProgressIndicator(modifier = barModifier)
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    modifier: Modifier = Modifier,
    selectionItems: Set<SelectionItem>,
    onRemoveRecord: () -> Unit,
    onDeletePermanently: () -> Unit,
    onCancel: () -> Unit,
) {
    val selectedVideos = selectionItems.filterIsInstance<SelectionItem.Video>()
    val selectedFolders = selectionItems.filterIsInstance<SelectionItem.Folder>()

    NextDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = when {
                    selectedVideos.isEmpty() -> when (selectedFolders.size) {
                        1 -> stringResource(R.string.delete_one_folder)

                        else -> stringResource(
                            R.string.delete_folders,
                            selectedFolders.size,
                        )
                    }

                    selectedFolders.isEmpty() -> when (selectedVideos.size) {
                        1 -> stringResource(R.string.delete_one_video)

                        else -> stringResource(
                            R.string.delete_videos,
                            selectedVideos.size,
                        )
                    }

                    else -> stringResource(
                        R.string.delete_items,
                        selectedFolders.size + selectedVideos.size,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDeletePermanently,
                modifier = modifier,
            ) {
                Text(text = stringResource(R.string.delete_permanently))
            }
        },
        dismissButton = {
            TextButton(onClick = onRemoveRecord) {
                Text(text = stringResource(R.string.delete_record))
            }
        },
        modifier = modifier,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.delete_record_info),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(R.string.delete_permanently_info),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        },
    )
}

@Composable
private fun SetupVaultPinDialog(onDismiss: () -> Unit, onPinConfirmed: (String) -> Unit) {
    var firstPin: String? by rememberSaveable { mutableStateOf(value = null) }
    var currentPin by rememberSaveable { mutableStateOf(value = "") }
    var errorCount by rememberSaveable { mutableStateOf(value = 0) }

    val isConfirmStep = firstPin != null
    val showError = errorCount > 0

    val context = LocalContext.current
    val isTv = remember { context.isTelevision }

    val onDigit: (Char) -> Unit = { digit ->
        if (currentPin.length < VAULT_PIN_LENGTH) {
            currentPin += digit
            if (currentPin.length == VAULT_PIN_LENGTH) {
                val pinJustEntered = currentPin
                if (!isConfirmStep) {
                    firstPin = pinJustEntered
                    currentPin = ""
                    errorCount = 0
                } else if (pinJustEntered == firstPin) {
                    onPinConfirmed(pinJustEntered)
                } else {
                    errorCount++
                    currentPin = ""
                }
            }
        }
    }
    val onBackspace: () -> Unit = {
        if (currentPin.isNotEmpty()) currentPin = currentPin.dropLast(n = 1)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = !isTv),
    ) {
        Surface(
            modifier = if (isTv) {
                Modifier.fillMaxWidth(
                    fraction = 0.72f,
                )
            } else {
                Modifier.fillMaxWidth()
            },
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            if (isTv) {
                // Landscape TV: info on the left, keypad on the right so it fits a short screen.
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SetupPinHeader(
                        modifier = Modifier.weight(weight = 1f),
                        isConfirmStep = isConfirmStep,
                        filledCount = currentPin.length,
                        showError = showError,
                    )
                    Column(
                        modifier = Modifier.weight(weight = 1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        PinKeypad(
                            modifier = Modifier.widthIn(max = 300.dp),
                            onDigit = onDigit,
                            onBackspace = onBackspace,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CancelButton(onClick = onDismiss)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 24.dp,
                            vertical = 28.dp,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    SetupPinHeader(
                        isConfirmStep = isConfirmStep,
                        filledCount = currentPin.length,
                        showError = showError,
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    PinKeypad(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        onDigit = onDigit,
                        onBackspace = onBackspace,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CancelButton(onClick = onDismiss)
                }
            }
        }
    }
}

@Composable
private fun SetupPinHeader(
    isConfirmStep: Boolean,
    filledCount: Int,
    showError: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Lock icon — matches the vault PIN screen's icon box
        Box(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = NextIcons.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = if (isConfirmStep) {
                stringResource(R.string.confirm_vault_pin)
            } else {
                stringResource(R.string.set_vault_pin)
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isConfirmStep) {
                stringResource(R.string.confirm_vault_pin_description)
            } else {
                stringResource(R.string.set_vault_pin_description)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
        PinDotsIndicator(
            filledCount = filledCount,
            error = showError,
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (showError) {
            Text(
                text = stringResource(R.string.pins_do_not_match),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SelectionActionsSheet(
    modifier: Modifier = Modifier,
    show: Boolean,
    contentFocusRequester: FocusRequester,
    showRenameAction: Boolean,
    showInfoAction: Boolean,
    showRateAction: Boolean,
    onPlayAction: () -> Unit,
    onHideAction: () -> Unit,
    onRenameAction: () -> Unit,
    onInfoAction: () -> Unit,
    onRateAction: () -> Unit,
    onDeleteAction: () -> Unit,
) {
    val actions = buildList {
        add(
            SelectionDockAction(
                icon = NextIcons.Play,
                label = stringResource(R.string.play),
                onClick = onPlayAction,
            ),
        )
        add(
            SelectionDockAction(
                icon = NextIcons.Lock,
                label = stringResource(R.string.encrypt),
                onClick = onHideAction,
            ),
        )
        if (showRenameAction) {
            add(
                SelectionDockAction(
                    icon = NextIcons.Edit,
                    label = stringResource(R.string.rename),
                    onClick = onRenameAction,
                ),
            )
        }
        if (showInfoAction) {
            add(
                SelectionDockAction(
                    icon = NextIcons.Info,
                    label = stringResource(R.string.info),
                    onClick = onInfoAction,
                ),
            )
        }
        if (showRateAction) {
            add(
                SelectionDockAction(
                    icon = NextIcons.Review,
                    label = stringResource(R.string.rate),
                    onClick = onRateAction,
                ),
            )
        }
        add(
            SelectionDockAction(
                icon = NextIcons.Delete,
                label = stringResource(R.string.delete),
                onClick = onDeleteAction,
                isDestructive = true,
            ),
        )
    }

    SelectionActionsDock(
        modifier = modifier,
        show = show,
        actions = actions,
        contentFocusRequester = contentFocusRequester,
    )
}

@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun MediaPickerScreenPreview(
    @PreviewParameter(VideoPickerPreviewParameterProvider::class)
    videos: List<Video>,
) {
    NextPlayerTheme {
        MediaPickerScreenContent(
            state = MediaPickerUiState(
                folderName = null,
                mediaDataState = DataState.Success(
                    value = MediaHolder(
                        folders = listOf(
                            Folder(
                                name = "Folder 1",
                                path = "/root/folder1",
                                dateModified = System.currentTimeMillis(),
                            ),
                            Folder(
                                name = "Folder 2",
                                path = "/root/folder2",
                                dateModified = System.currentTimeMillis(),
                            ),
                        ),
                        videos = videos,
                    ),
                ),
                preferences = ApplicationPreferences().copy(
                    mediaViewMode = MediaViewMode.FOLDER_TREE,
                    mediaLayoutMode = MediaLayoutMode.GRID,
                ),
            ),
        )
    }
}

@Preview
@Composable
private fun ButtonPreview() {
    Surface {
        TextIconToggleButton(
            text = "Title",
            icon = NextIcons.Title,
            onClick = {},
        )
    }
}

@DayNightPreview
@Composable
private fun MediaPickerNoVideosFoundPreview() {
    NextPlayerTheme {
        Surface {
            MediaPickerScreenContent(
                state = MediaPickerUiState(
                    folderName = null,
                    mediaDataState = DataState.Success(value = null),
                    preferences = ApplicationPreferences(),
                ),
            )
        }
    }
}

@DayNightPreview
@Composable
private fun MediaPickerLoadingPreview() {
    NextPlayerTheme {
        Surface {
            MediaPickerScreenContent(
                state = MediaPickerUiState(
                    folderName = null,
                    mediaDataState = DataState.Loading,
                    preferences = ApplicationPreferences(),
                ),
            )
        }
    }
}
