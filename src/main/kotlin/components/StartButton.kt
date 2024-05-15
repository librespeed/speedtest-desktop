package components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import theme.ColorBox
import theme.Fonts

@Composable
fun StartButton(
    modifier: Modifier = Modifier,
) {

    val mainStrokeSize = 5f
    val style = TextStyle(
        fontSize = 28.sp,
        color = ColorBox.text,
        fontFamily = Fonts.open_sans
    )
    val textLayoutResult = rememberTextMeasurer().measure("Start", style)

    val infiniteAnimation = rememberInfiniteTransition()
    val ticker by infiniteAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val gradient = remember { Brush.linearGradient(colors = listOf(Color(0xFF7E60AA), ColorBox.primary)) }

    Canvas(modifier) {
        drawCircle(brush = gradient, style = Stroke(mainStrokeSize.dp.toPx()), radius = (size.minDimension / 2f) * 0.8f)

        val radius = lerp((size.minDimension / 2f) * 0.8f, (size.minDimension / 2f), ticker)
        val strokeSize = lerp(mainStrokeSize, 0f, ticker)
        drawCircle(brush = gradient, radius = radius, style = Stroke(strokeSize.dp.toPx()))
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                x = center.x - textLayoutResult.size.width / 2,
                y = center.y - textLayoutResult.size.height / 2,
            )
        )
    }

}