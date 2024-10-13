package routes.dialogs

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import theme.ColorBox

@Composable
fun BaseDialog(
    expanded: Boolean,
    onDismissRequest: (() -> Unit)? = null,
    backgroundColor: Color = ColorBox.card,
    content: @Composable BoxScope.() -> Unit
) {
    val expandedState = remember { MutableTransitionState(false) }
    expandedState.targetState = expanded

    val transition = updateTransition(expandedState, "CustomDialog")
    val scale by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                tween(
                    durationMillis = 200
                )
            } else {
                tween(
                    durationMillis = 200
                )
            }
        }
    ) {
        if (it) {
            1f
        } else {
            0.8f
        }
    }
    val alpha by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                tween(durationMillis = 200)
            } else {
                tween(durationMillis = 200)
            }
        }
    ) {
        if (it) {
            1f
        } else {
            0f
        }
    }

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
            Box(
                modifier = Modifier
                    .alpha(alpha)
                    .fillMaxSize()
                    .focusable(false)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        onDismissRequest?.invoke()
                    },
                contentAlignment = Alignment.Center,
                content = {
                    Box(
                        modifier = Modifier
                            .alpha(alpha)
                            .scale(scale)
                            .padding(top = 25.dp, bottom = 25.dp)
                            .focusable(false)
                            .surface(shape = RoundedCornerShape(24.dp), backgroundColor = backgroundColor, elevation = 8.dp)
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
                        content = content
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
