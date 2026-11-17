package dev.anilbeesetti.nextplayer.navigation

import androidx.compose.runtime.mutableIntStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TopLevelNavigationTest {

    @Test
    fun topLevelDestinationsAreInOrder() {
        assertEquals(
            listOf(
                TopLevelDestination.MEDIA,
                TopLevelDestination.MORE,
            ),
            TopLevelDestination.entries,
        )
    }

    @Test
    fun topLevelContentKeysContainsDestinations() {
        val stacks = TopLevelDestination.entries.associate { destination ->
            destination.route to NavBackStack<NavKey>(destination.route)
        }
        val state = TopLevelNavState(
            destinations = TopLevelDestination.entries,
            backStacks = stacks,
            selectedIndexState = mutableIntStateOf(value = 0),
        )
        val provider = entryProvider<NavKey> {
            TopLevelDestination.entries.forEach { destination ->
                entry(key = destination.route) {}
            }
        }
        for (dest in TopLevelDestination.entries) {
            assertTrue(state.topLevelContentKeys.contains(dest.route))
            assertTrue(state.topLevelContentKeys.contains(provider(dest.route).contentKey))
        }
    }
}
