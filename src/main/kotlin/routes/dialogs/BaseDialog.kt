package routes.dialogs

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

@Composable
fun BaseDialog(
    expanded: Boolean,
    onDismissRequest: (() -> Unit)? = null,
    backgroundColor: Color,
    enter: EnterTransition = fadeIn(),
    exit: ExitTransition = fadeOut(),
    content: @Composable BoxScope.() -> Unit
) {
    val expandedState = remember { MutableTransitionState(false) }
    expandedState.targetState = expanded

    if (expandedState.currentState || expandedState.targetState || !expandedState.isIdle) {
        Popup(
            properties = PopupProperties(focusable = true),
            popupPositionProvider  = object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset = IntOffset.Zero
            },
            onDismissRequest = onDismissRequest
        ) {
            AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                visibleState = expandedState,
                enter = enter,
                exit = exit,
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                onDismissRequest?.invoke()
                            },
                        contentAlignment = Alignment.Center,
                        content = {
                            Box(
                                modifier = Modifier
                                    .surface(shape = RoundedCornerShape(16.dp), backgroundColor = backgroundColor, elevation = 8.dp)
                                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
                                content = content
                            )
                        }
                    )
                }
            )
        }
    }
}


private fun Modifier.surface(
    shape: Shape,
    backgroundColor: Color,
    elevation: Dp
) = this.shadow(elevation, shape, clip = false)
    .background(color = backgroundColor, shape = shape)
    .clip(shape)
