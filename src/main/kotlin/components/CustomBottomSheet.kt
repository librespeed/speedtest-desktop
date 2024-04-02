package components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import theme.ColorBox

@Composable
fun CustomBottomSheet(
    scope: CoroutineScope,
    state: BottomSheetState,
    sheetContent : @Composable BoxScope.() -> Unit,
    content : @Composable () -> Unit
) {

    val slideAnim = animateFloatAsState(if (state.isOpen) 1f else 0f)

    Box(modifier = Modifier.fillMaxSize()) {
        content()
        DimView(
            slideAnim.value,
            onDimClicked = {
                scope.launch {
                    state.close()
                }
            }
        )
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = state.isOpen,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = 400.dp)
                    .heightIn(max = 600.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topEnd = 20.dp, topStart = 20.dp))
                    .background(ColorBox.card), content = sheetContent
            )
        }
    }

}

class BottomSheetState {
    private var state = mutableStateOf(false)
    val isOpen: Boolean
        get() = state.value
    fun open() {
        state.value = true
    }
    fun close() {
        state.value = false
    }
}

@Composable
fun rememberBottomSheetState(): BottomSheetState {
    return rememberSaveable {
        BottomSheetState()
    }
}

@Composable
private fun DimView(fraction: Float, onDimClicked: () -> Unit) {
    if (fraction == 1f) {
        Canvas(
            modifier = Modifier.fillMaxSize()
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                    onDimClicked.invoke()
                }) {
            drawRect(Color.Black.copy(0.6f), alpha = fraction)
        }
    } else {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(Color.Black.copy(0.6f), alpha = fraction)
        }
    }
}