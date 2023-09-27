package theme

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppRippleTheme : RippleTheme {

    private const val DraggedStateLayerOpacity = 0.16f
    private const val FocusStateLayerOpacity = 0.12f
    private const val HoverStateLayerOpacity = 0.08f
    private const val PressedStateLayerOpacity = 0.10f

    @Composable
    override fun defaultColor(): Color {
        return ColorBox.text.copy(0.8f)
    }

    @Composable
    override fun rippleAlpha(): RippleAlpha {
        return DefaultRippleAlpha
    }

    private val DefaultRippleAlpha = RippleAlpha(
        pressedAlpha = PressedStateLayerOpacity,
        focusedAlpha = FocusStateLayerOpacity,
        draggedAlpha = DraggedStateLayerOpacity,
        hoveredAlpha = HoverStateLayerOpacity
    )

}