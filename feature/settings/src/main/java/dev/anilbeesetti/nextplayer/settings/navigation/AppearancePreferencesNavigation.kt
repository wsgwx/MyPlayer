package dev.anilbeesetti.nextplayer.settings.navigation

import androidx.compose.runtime.SideEffect
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.anilbeesetti.nextplayer.settings.screens.appearance.AppearancePreferencesScreen
import dev.anilbeesetti.nextplayer.settings.screens.appearance.AppearancePreferencesViewModel
import dev.anilbeesetti.nextplayer.settings.screens.appearance.BackgroundPreferencesScreen
import dev.anilbeesetti.nextplayer.settings.screens.appearance.BackgroundPreferencesViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
object AppearancePreferencesRoute : NavKey

@Serializable
object BackgroundPreferencesRoute : NavKey

fun NavBackStack<NavKey>.navigateToAppearancePreferences() {
    add(element = AppearancePreferencesRoute)
}

fun NavBackStack<NavKey>.navigateToBackgroundPreferences() {
    add(element = BackgroundPreferencesRoute)
}

fun EntryProviderScope<NavKey>.appearancePreferencesEntry(
    onNavigateUp: () -> Unit,
    onBackgroundClick: () -> Unit,
) {
    entry<AppearancePreferencesRoute> {
        val output = AppearancePreferencesViewModel.Output(
            navigateUp = onNavigateUp,
            openBackground = onBackgroundClick,
        )
        val viewModel = koinViewModel<AppearancePreferencesViewModel>(
            parameters = { parametersOf(output) },
        )
        SideEffect { viewModel.output = output }
        AppearancePreferencesScreen(viewModel = viewModel)
    }
}

fun EntryProviderScope<NavKey>.backgroundPreferencesEntry(onNavigateUp: () -> Unit) {
    entry<BackgroundPreferencesRoute> {
        val output = BackgroundPreferencesViewModel.Output(navigateUp = onNavigateUp)
        val viewModel = koinViewModel<BackgroundPreferencesViewModel>(
            parameters = { parametersOf(output) },
        )
        SideEffect { viewModel.output = output }
        BackgroundPreferencesScreen(viewModel = viewModel)
    }
}
