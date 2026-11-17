package dev.anilbeesetti.nextplayer.settings.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.anilbeesetti.nextplayer.core.model.ThemePalette
import dev.anilbeesetti.nextplayer.core.ui.R

@Composable
fun ThemePalette.name(): String {
    val stringRes = when (this) {
        ThemePalette.CINEMA -> R.string.palette_cinema
        ThemePalette.AMBER -> R.string.palette_amber
        ThemePalette.TANGERINE -> R.string.palette_tangerine
        ThemePalette.OCEAN -> R.string.palette_ocean
        ThemePalette.FOREST -> R.string.palette_forest
    }
    return stringResource(id = stringRes)
}

@Composable
fun ThemePalette.description(): String {
    val stringRes = when (this) {
        ThemePalette.CINEMA -> R.string.palette_cinema_desc
        ThemePalette.AMBER -> R.string.palette_amber_desc
        ThemePalette.TANGERINE -> R.string.palette_tangerine_desc
        ThemePalette.OCEAN -> R.string.palette_ocean_desc
        ThemePalette.FOREST -> R.string.palette_forest_desc
    }
    return stringResource(id = stringRes)
}
