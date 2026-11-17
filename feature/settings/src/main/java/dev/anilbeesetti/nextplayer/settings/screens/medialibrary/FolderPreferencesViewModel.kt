package dev.anilbeesetti.nextplayer.settings.screens.medialibrary

import android.content.Context
import androidx.lifecycle.viewModelScope
import dev.anilbeesetti.nextplayer.core.common.extensions.getPathFromTreeUri
import dev.anilbeesetti.nextplayer.core.common.service.system.SystemService
import dev.anilbeesetti.nextplayer.core.data.repository.MediaRepository
import dev.anilbeesetti.nextplayer.core.data.repository.PreferencesRepository
import dev.anilbeesetti.nextplayer.core.model.ApplicationPreferences
import dev.anilbeesetti.nextplayer.core.model.Folder
import dev.anilbeesetti.nextplayer.core.ui.base.DataState
import dev.anilbeesetti.nextplayer.core.ui.base.MviViewModel
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class FolderPreferencesViewModel(
    mediaRepository: MediaRepository,
    private val preferencesRepository: PreferencesRepository,
    private val systemService: SystemService,
    private val context: Context,
    @InjectedParam internal var output: Output,
) : MviViewModel<FolderPreferencesUiState, FolderPreferencesUiEvent>() {

    data class Output(val navigateUp: () -> Unit)

    private val stateInternal = MutableStateFlow(
        value = FolderPreferencesUiState(
            preferences = preferencesRepository.applicationPreferences.value,
        ),
    )
    override val state: StateFlow<FolderPreferencesUiState> = stateInternal.asStateFlow()

    init {
        // MediaStore only reports folders that already contain videos, so folders the user picked by
        // hand are merged in to keep them visible (and removable) even while they are still empty.
        viewModelScope.launch {
            combine(
                mediaRepository.observeFolders(),
                preferencesRepository.applicationPreferences,
            ) { discoveredFolders, preferences ->
                val visibleDiscovered = discoveredFolders.filter { folder ->
                    folder.path !in preferences.hiddenScanFolders
                }
                val custom = preferences.scanFolders
                    .filter { path -> visibleDiscovered.none { it.path == path } }
                    .map { path ->
                        Folder(
                            path = path,
                            name = File(path).name.ifBlank { path },
                            dateModified = 0L,
                            totalSize = 0L,
                            totalDuration = 0L,
                            videosCount = 0,
                            foldersCount = 0,
                        )
                    }
                (visibleDiscovered + custom).sortedBy { it.name.lowercase() }
            }.collect { folders ->
                stateInternal.update { currentState ->
                    currentState.copy(foldersDataState = DataState.Success(value = folders))
                }
            }
        }

        viewModelScope.launch {
            preferencesRepository.applicationPreferences.collect { preferences ->
                stateInternal.update { currentState ->
                    currentState.copy(preferences = preferences)
                }
            }
        }
    }

    override fun onAction(action: FolderPreferencesUiEvent) {
        when (action) {
            is FolderPreferencesUiEvent.NavigateUp -> output.navigateUp()
            is FolderPreferencesUiEvent.UpdateExcludeList -> toggleScanFolder(action.path)
            is FolderPreferencesUiEvent.RemoveFolder -> removeFolder(action.path)
            is FolderPreferencesUiEvent.AddFolder -> addFolder()
        }
    }

    private fun addFolder() {
        viewModelScope.launch {
            val treeUri = systemService.pickFolder() ?: return@launch
            val path = context.getPathFromTreeUri(uri = treeUri) ?: return@launch
            preferencesRepository.updateApplicationPreferences {
                if (path in it.scanFolders) {
                    it
                } else {
                    it.copy(
                        scanFolders = it.scanFolders + path,
                        hiddenScanFolders = it.hiddenScanFolders - path,
                    )
                }
            }
        }
    }

    /** Removes a folder from the list: unchecks it and keeps it out of the list afterwards. */
    private fun removeFolder(path: String) {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(
                    scanFolders = it.scanFolders - path,
                    hiddenScanFolders = it.hiddenScanFolders + path,
                )
            }
        }
    }

    private fun toggleScanFolder(path: String) {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences {
                it.copy(
                    scanFolders = if (path in it.scanFolders) {
                        it.scanFolders - path
                    } else {
                        it.scanFolders + path
                    },
                )
            }
        }
    }
}

data class FolderPreferencesUiState(
    val foldersDataState: DataState<List<Folder>> = DataState.Loading,
    val preferences: ApplicationPreferences = ApplicationPreferences(),
)

sealed interface FolderPreferencesUiEvent {
    data object NavigateUp : FolderPreferencesUiEvent

    data class UpdateExcludeList(val path: String) : FolderPreferencesUiEvent

    data class RemoveFolder(val path: String) : FolderPreferencesUiEvent

    data object AddFolder : FolderPreferencesUiEvent
}
