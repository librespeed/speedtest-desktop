package components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PaintStrokeCap
import theme.ColorBox
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun SpeedMeterView(
    speed : Float = 0f,
    progress : Float = 0f,
    modifier: Modifier = Modifier
) {

    val paint = Paint()
    val frameworkPaint = paint.asFrameworkPaint()
    frameworkPaint.mode = PaintMode.STROKE
    frameworkPaint.strokeWidth = 3.dp.value
    frameworkPaint.strokeCap = PaintStrokeCap.ROUND
    frameworkPaint.maskFilter = MaskFilter.makeBlur(FilterBlurMode.SOLID, 10f)

    val animateFrac = animateFloatAsState(min(speed,1f))
    val progressFrac = animateFloatAsState(progress)

    Canvas(modifier) {

        frameworkPaint.shader = LinearGradientShader(
            from = Offset(0f,size.height/2f),
            to = Offset(size.width,size.height/2f),
            colors = listOf(Color(0xFFe47d23), Color(0xFFe8fb2a), Color(0xFF00ffa6))
        )

        val radius = (size.width / 2)

        var lineHeight: Float
        for (i in 155..385 step 5) {
            lineHeight = if (i % 4 == 0) {
                16f
            } else {
                10f
            }
            drawLines(i.toFloat(),radius, lineHeight)
        }
        if (animateFrac.value > 0f) {
            val progressVal = lerp(155f,385f,animateFrac.value).toInt()
            for (i in 155..progressVal step 5) {
                lineHeight = if (i % 4 == 0) {
                    16f
                } else {
                    10f
                }
                drawLinesProgress(i.toFloat(),radius, lineHeight, paint)
            }
        }
        drawArc(
            color = ColorBox.text,
            startAngle = 155f,
            sweepAngle = 230f,
            useCenter = false,
            topLeft = Offset(
                x = 35f,
                y = 35f
            ),
            size = Size(
                width = size.width - 70f,
                height = size.height - 70f
            ),
            style = Stroke(1.dp.toPx(), cap = StrokeCap.Round),
            alpha = 0.2f
        )
        drawArc(
            color = ColorBox.text,
            startAngle = 155f,
            sweepAngle = progressFrac.value * 230f,
            useCenter = false,
            topLeft = Offset(
                x = 35f,
                y = 35f
            ),
            size = Size(
                width = size.width - 70f,
                height = size.height - 70f
            ),
            style = Stroke(2.dp.toPx(), cap = StrokeCap.Round),
        )
    }

}

private fun DrawScope.drawLines (degree : Float,radius : Float,lineHeight : Float) {
    val calcOffsets = calcOffsets(center,degree, radius, lineHeight)
    val start = calcOffsets.first
    val end = calcOffsets.second
    drawLine(
        color = ColorBox.text.copy(0.5f),
        start = start,
        end = end,
        strokeWidth = 2.dp.toPx()
    )
}

private fun DrawScope.drawLinesProgress (degree : Float,radius : Float,lineHeight : Float,paint : Paint) {
    val calcOffsets = calcOffsets(center,degree, radius, lineHeight)
    val start = calcOffsets.first
    val end = calcOffsets.second
    drawIntoCanvas {
        it.drawLine(
            p1 = start,
            p2 = end,
            paint = paint
        )
    }
}

private fun calcOffsets (center : Offset,degree: Float,radius: Float,lineHeight: Float) : Pair<Offset,Offset> {
    val angle = degree * (PI / 180)
    val startX = center.x + radius * cos(angle)
    val startY = center.y + radius * sin(angle)
    val endX = center.x + (radius - lineHeight) * cos(angle)
    val endY = center.y + (radius - lineHeight) * sin(angle)
    return Pair(Offset(startX.toFloat(), startY.toFloat()),Offset(endX.toFloat(), endY.toFloat()))
}