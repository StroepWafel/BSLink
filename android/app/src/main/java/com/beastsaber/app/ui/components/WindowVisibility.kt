package com.beastsaber.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Invokes [onVisibilityChange] when this composable intersects the device window (any overlap).
 * Used to pause media when the user scrolls content off-screen.
 */
@Composable
fun TrackWindowVisibility(
    onVisibilityChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val config = LocalConfiguration.current
    val density = LocalDensity.current
    var last: Boolean? by remember { mutableStateOf(null) }
    Box(
        modifier
            .onGloballyPositioned { coordinates ->
                val bounds = coordinates.boundsInWindow()
                val w = with(density) { config.screenWidthDp.dp.toPx() }
                val h = with(density) { config.screenHeightDp.dp.toPx() }
                val screenRect = Rect(0f, 0f, w, h)
                val visible = bounds.overlaps(screenRect) && bounds.width > 0f && bounds.height > 0f
                if (last != visible) {
                    last = visible
                    onVisibilityChange(visible)
                }
            }
    ) {
        content()
    }
}
