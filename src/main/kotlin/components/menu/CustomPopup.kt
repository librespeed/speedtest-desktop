package components.menu

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.rememberCursorPositionProvider
import theme.ColorBox

@Suppress("ModifierParameter")
@Composable
internal fun DropdownMenuContent(
    expandedStates: MutableTransitionState<Boolean>,
    transformOriginState: MutableState<TransformOrigin>,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    // Menu open/close animation.
    val transition = updateTransition(expandedStates, "DropDownMenu")

    val scale by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                tween(
                    durationMillis = 200,
                    easing = LinearOutSlowInEasing
                )
            } else {
                tween(
                    durationMillis = 200,
                    easing = LinearOutSlowInEasing
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
                // Dismissed to expanded
                tween(durationMillis = 120)
            } else {
                // Expanded to dismissed.
                tween(durationMillis = 120)
            }
        }
    ) {
        if (it) {
            // Menu is expanded.
            1f
        } else {
            // Menu is dismissed.
            0f
        }
    }
    Card(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
            transformOrigin = transformOriginState.value
        },
        colors = CardDefaults.cardColors(containerColor = ColorBox.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = modifier
                .padding(vertical = 8.dp)
                .width(IntrinsicSize.Max)
                .verticalScroll(rememberScrollState()),
            content = content
        )
    }
}

@Composable
private fun OpenDropdownMenu(
    expandedStates: MutableTransitionState<Boolean>,
    popupPositionProvider: PopupPositionProvider,
    transformOriginState: MutableState<TransformOrigin> =
        remember { mutableStateOf(TransformOrigin.Center) },
    onDismissRequest: () -> Unit,
    focusable: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
){
    var focusManager: FocusManager? by mutableStateOf(null)
    var inputModeManager: InputModeManager? by mutableStateOf(null)
    Popup(
        properties = PopupProperties(focusable = focusable),
        onDismissRequest = onDismissRequest,
        popupPositionProvider = popupPositionProvider,
    ) {
        focusManager = LocalFocusManager.current
        inputModeManager = LocalInputModeManager.current

        DropdownMenuContent(
            expandedStates = expandedStates,
            transformOriginState = transformOriginState,
            modifier = modifier,
            content = content
        )
    }
}

@Composable
fun CustomDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    focusable: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val expandedStates = remember { MutableTransitionState(false) }
    expandedStates.targetState = expanded

    if (expanded) {
        //handle outside click
        Popup(
            popupPositionProvider = object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset = IntOffset.Zero
            },
            properties = PopupProperties(focusable = true),
            onDismissRequest = {},
        ) {
            Box(
                Modifier.fillMaxSize()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        onDismissRequest.invoke()
                    })
        }
    }

    if (expandedStates.currentState || expandedStates.targetState) {
        OpenDropdownMenu(
            expandedStates = expandedStates,
            popupPositionProvider = rememberCursorPositionProvider(),
            onDismissRequest = onDismissRequest,
            focusable = focusable,
            modifier = modifier,
            content = content
        )
    }
}