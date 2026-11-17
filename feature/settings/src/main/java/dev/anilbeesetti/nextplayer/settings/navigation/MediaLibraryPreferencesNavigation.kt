package dev.anilbeesetti.nextplayer.settings.navigation

import androidx.compose.runtime.SideEffect
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.anilbeesetti.nextplayer.settings.screens.medialibrary.FolderPreferencesScreen
import dev.anilbeesetti.nextplayer.settings.screens.medialibrary.FolderPreferencesViewModel
import dev.anilbeesetti.nextplayer.settings.screens.medialibrary.MediaLibraryPreferencesScreen
import dev.anilbeesetti.nextplayer.settings.screens.medialibrary.MediaLibraryPreferencesViewModel
import dev.anilbeesetti.nextplayer.settings.screens.removed.RemovedVideosScreen
import dev.anilbeesetti.nextplayer.settings.screens.removed.RemovedVideosViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
object MediaLibraryPreferencesRoute : NavKey

@Serializable
object FolderPreferencesRoute : NavKey

@Serializable
object RemovedRecordsRoute : NavKey

fun NavBackStack<NavKey>.navigateToMediaLibraryPreferencesScreen() {
    add(element = MediaLibraryPreferencesRoute)
}

fun NavBackStack<NavKey>.navigateToFolderPreferencesScreen() {
    add(element = FolderPreferencesRoute)
}

fun NavBackStack<NavKey>.navigateToRemovedRecordsScreen() {
    add(element = RemovedRecordsRoute)
}

fun EntryProviderScope<NavKey>.mediaLibraryPreferencesEntry(
    onNavigateUp: () -> Unit,
    onFolderSettingClick: () -> Unit,
    onThumbnailSettingClick: () -> Unit,
    onRemovedRecordsClick: () -> Unit,
) {
    entry<MediaLibraryPreferencesRoute> {
        val output = MediaLibraryPreferencesViewModel.Output(
            navigateUp = onNavigateUp,
            openFolders = onFolderSettingClick,
            openThumbnails = onThumbnailSettingClick,
            openRemovedRecords = onRemovedRecordsClick,
        )
        val viewModel = koinViewModel<MediaLibraryPreferencesViewModel>(
            parameters = { parametersOf(output) },
        )
        SideEffect { viewModel.output = output }
        MediaLibraryPreferencesScreen(viewModel = viewModel)
    }
}

fun EntryProviderScope<NavKey>.folderPreferencesEntry(onNavigateUp: () -> Unit) {
    entry<FolderPreferencesRoute> {
        val output = FolderPreferencesViewModel.Output(navigateUp = onNavigateUp)
        val viewModel = koinViewModel<FolderPreferencesViewModel>(
            parameters = { parametersOf(output) },
        )
        SideEffect { viewModel.output = output }
        FolderPreferencesScreen(viewModel = viewModel)
    }
}

fun EntryProviderScope<NavKey>.removedRecordsEntry(onNavigateUp: () -> Unit) {
    entry<RemovedRecordsRoute> {
        val output = RemovedVideosViewModel.Output(navigateUp = onNavigateUp)
        val viewModel = koinViewModel<RemovedVideosViewModel>(
            parameters = { parametersOf(output) },
        )
        SideEffect { viewModel.output = output }
        RemovedVideosScreen(viewModel = viewModel)
    }
}
