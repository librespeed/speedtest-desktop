package theme

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RippleConfiguration

@OptIn(ExperimentalMaterial3Api::class)
val rippleConfiguration = RippleConfiguration(
    color = ColorBox.text.copy(0.8f), RippleAlpha(
        pressedAlpha = 0.10f,
        focusedAlpha = 0.12f,
        draggedAlpha = 0.16f,
        hoveredAlpha = 0.08f
    )
)