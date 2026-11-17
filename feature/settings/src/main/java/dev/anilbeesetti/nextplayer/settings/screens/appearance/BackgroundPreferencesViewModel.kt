package dev.anilbeesetti.nextplayer.settings.screens.appearance

import android.net.Uri
import androidx.lifecycle.viewModelScope
import dev.anilbeesetti.nextplayer.core.common.service.system.SystemService
import dev.anilbeesetti.nextplayer.core.data.repository.PreferencesRepository
import dev.anilbeesetti.nextplayer.core.model.ApplicationPreferences
import dev.anilbeesetti.nextplayer.core.model.BackgroundConfig
import dev.anilbeesetti.nextplayer.core.model.BackgroundType
import dev.anilbeesetti.nextplayer.core.ui.base.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class BackgroundPreferencesViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val systemService: SystemService,
    @InjectedParam internal var output: Output,
) : MviViewModel<BackgroundPreferencesUiState, BackgroundPreferencesUiEvent>() {

    data class Output(val navigateUp: () -> Unit)

    private val stateInternal = MutableStateFlow(value = BackgroundPreferencesUiState())
    override val state: StateFlow<BackgroundPreferencesUiState> = stateInternal.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.applicationPreferences.collect { preferences ->
                stateInternal.update { currentState ->
                    currentState.copy(background = preferences.background)
                }
            }
        }
    }

    override fun onAction(action: BackgroundPreferencesUiEvent) {
        when (action) {
            is BackgroundPreferencesUiEvent.NavigateUp -> output.navigateUp()
            is BackgroundPreferencesUiEvent.SelectType -> selectType(action.type)
            is BackgroundPreferencesUiEvent.PickImage -> pickImage()
            is BackgroundPreferencesUiEvent.ClearImage -> update {
                it.copy(type = BackgroundType.DEFAULT, imageUri = null)
            }
        }
    }

    private fun selectType(type: BackgroundType) {
        when (type) {
            BackgroundType.IMAGE -> pickImage()
            else -> update { it.copy(type = type) }
        }
    }

    private fun pickImage() {
        viewModelScope.launch {
            val uri: Uri = systemService.pickImage() ?: return@launch
            update { it.copy(type = BackgroundType.IMAGE, imageUri = uri.toString()) }
        }
    }

    private fun update(transform: (BackgroundConfig) -> BackgroundConfig) {
        viewModelScope.launch {
            preferencesRepository.updateApplicationPreferences { preferences: ApplicationPreferences ->
                preferences.copy(background = transform(preferences.background))
            }
        }
    }
}

data class BackgroundPreferencesUiState(
    val background: BackgroundConfig = BackgroundConfig(),
)

sealed interface BackgroundPreferencesUiEvent {
    data object NavigateUp : BackgroundPreferencesUiEvent
    data class SelectType(val type: BackgroundType) : BackgroundPreferencesUiEvent
    data object PickImage : BackgroundPreferencesUiEvent
    data object ClearImage : BackgroundPreferencesUiEvent
}
