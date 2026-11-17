package dev.anilbeesetti.nextplayer.settings.screens.removed

import androidx.lifecycle.viewModelScope
import dev.anilbeesetti.nextplayer.core.data.repository.RemovedVideoRepository
import dev.anilbeesetti.nextplayer.core.ui.base.MviViewModel
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class RemovedVideosViewModel(
    private val removedVideoRepository: RemovedVideoRepository,
    @InjectedParam internal var output: Output,
) : MviViewModel<RemovedVideosUiState, RemovedVideosUiEvent>() {

    data class Output(val navigateUp: () -> Unit)

    private val stateInternal = MutableStateFlow(value = RemovedVideosUiState())
    override val state: StateFlow<RemovedVideosUiState> = stateInternal.asStateFlow()

    init {
        viewModelScope.launch {
            removedVideoRepository.observeRemovedVideos().collect { removedVideos ->
                stateInternal.update { currentState ->
                    currentState.copy(
                        videos = removedVideos.map { removedVideo ->
                            RemovedVideoItem(
                                uriString = removedVideo.uriString,
                                name = File(removedVideo.path).name.ifBlank { removedVideo.path },
                                path = removedVideo.path,
                            )
                        },
                    )
                }
            }
        }
    }

    override fun onAction(action: RemovedVideosUiEvent) {
        when (action) {
            is RemovedVideosUiEvent.NavigateUp -> output.navigateUp()

            is RemovedVideosUiEvent.Restore -> restore(listOf(element = action.uriString))

            is RemovedVideosUiEvent.RestoreAll -> restore(
                uris = stateInternal.value.videos.map { it.uriString },
            )
        }
    }

    private fun restore(uris: List<String>) {
        viewModelScope.launch { removedVideoRepository.restoreRemovedVideos(uris = uris) }
    }
}

data class RemovedVideoItem(
    val uriString: String,
    val name: String,
    val path: String,
)

data class RemovedVideosUiState(
    val videos: List<RemovedVideoItem> = emptyList(),
)

sealed interface RemovedVideosUiEvent {
    data object NavigateUp : RemovedVideosUiEvent
    data class Restore(val uriString: String) : RemovedVideosUiEvent
    data object RestoreAll : RemovedVideosUiEvent
}
