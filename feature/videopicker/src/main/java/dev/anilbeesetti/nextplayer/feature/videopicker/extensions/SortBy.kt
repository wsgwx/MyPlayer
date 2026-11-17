package dev.anilbeesetti.nextplayer.feature.videopicker.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.anilbeesetti.nextplayer.core.model.Sort
import dev.anilbeesetti.nextplayer.core.ui.R

@Composable
fun Sort.By.name(): String = when (this) {
    Sort.By.TITLE -> stringResource(id = R.string.title)
    Sort.By.LENGTH -> stringResource(id = R.string.duration)
    Sort.By.PATH -> stringResource(id = R.string.path)
    Sort.By.SIZE -> stringResource(id = R.string.size)
    Sort.By.DATE -> stringResource(id = R.string.date)
    Sort.By.RATING -> stringResource(id = R.string.rating_score)
}
