package dev.anilbeesetti.nextplayer.core.ui.components

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange

/**
 * Observes horizontal drags on the whole surface without consuming them, so scrolling and item
 * gestures keep working untouched (events are read in the initial pass, before children see them).
 *
 * A swipe fires as soon as the finger has travelled [thresholdFraction] of the width, so entering
 * the privacy space feels immediate instead of waiting for the finger to lift.
 */
fun Modifier.horizontalSwipe(
    enabled: Boolean = true,
    thresholdFraction: Float = 0.16f,
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
): Modifier = if (!enabled || (onSwipeLeft == null && onSwipeRight == null)) {
    this
} else {
    this.pointerInput(
        enabled,
        thresholdFraction,
        onSwipeLeft,
        onSwipeRight,
    ) {
        awaitPointerEventScope {
            while (true) {
                val down = awaitFirstDown(
                    requireUnconsumed = false,
                    pass = PointerEventPass.Initial,
                )
                var travelled = 0f
                var fired = false
                while (!fired) {
                    val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    travelled += change.positionChange().x
                    val limit = size.width * thresholdFraction
                    when {
                        travelled <= -limit -> {
                            fired = true
                            onSwipeLeft?.invoke()
                        }

                        travelled >= limit -> {
                            fired = true
                            onSwipeRight?.invoke()
                        }

                        !change.pressed -> break
                    }
                }
            }
        }
    }
}
