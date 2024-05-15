package components

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.animateSizeAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import theme.ColorBox
import theme.Fonts

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SwitchUnit(modifier: Modifier, isMbps : Boolean, onClicked : (Int) -> Unit) {

    val style = TextStyle(
        color = ColorBox.text,
        fontFamily = Fonts.open_sans
    )

    val textMbps = rememberTextMeasurer().measure("Mbps", style = style)
    val textMbs = rememberTextMeasurer().measure("MB/s", style = style)

    var viewWidth = remember { 0 }
    val viewHeightPads = remember { 38f }

    val animOffset = animateOffsetAsState(
        if (isMbps) Offset(x = 3f, y = 3f)
        else Offset(x = textMbps.size.width + 40f, y = 3f)
    )
    val animSize = animateSizeAsState(
        if (isMbps) Size(
            width = 17f + textMbps.size.width + 20f,
            height = viewHeightPads
        ) else Size(width = 20f + textMbs.size.width + 17f, height = viewHeightPads)
    )

    Canvas(modifier = modifier.width((textMbps.size.width + textMbs.size.width + 80f).dp)
        .height(44.dp)
        .clip(RoundedCornerShape(50))
        .background(ColorBox.card)
        .onSizeChanged {
            viewWidth = it.width
        }
        .pointerHoverIcon(PointerIcon.Hand)
        .onPointerEvent(PointerEventType.Press) {
            onClicked.invoke(if (it.changes.first().position.x > (viewWidth / 2f)) 1 else 0)
        }
    ) {
        drawText(
            textLayoutResult = textMbps,
            topLeft = Offset(
                x = 20f,
                y = center.y - textMbps.size.height / 2,
            )
        )
        drawText(
            textLayoutResult = textMbs,
            topLeft = Offset(
                x = 40f + textMbps.size.width + 20f,
                y = center.y - textMbs.size.height / 2,
            )
        )
        drawRoundRect(
            color = ColorBox.text.copy(0.1f),
            cornerRadius = CornerRadius(50f, 50f),
            topLeft = animOffset.value,
            size = animSize.value
        )
    }

}