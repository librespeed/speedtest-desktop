package components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PaintStrokeCap
import theme.ColorBox
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProgressView(
    progress : Float = 0f,
    modifier: Modifier = Modifier
) {

    val paint = Paint()
    val frameworkPaint = paint.asFrameworkPaint()
    frameworkPaint.mode = PaintMode.STROKE
    frameworkPaint.strokeWidth = 3.dp.value
    frameworkPaint.strokeCap = PaintStrokeCap.ROUND
    frameworkPaint.maskFilter = MaskFilter.makeBlur(FilterBlurMode.SOLID, 10f)

    val animateFrac = animateFloatAsState(progress)

    Canvas(modifier) {

        frameworkPaint.shader = LinearGradientShader(
            from = Offset(0f,size.height/2f),
            to = Offset(size.width,size.height/2f),
            colors = listOf(Color(0xFFe47d23), Color(0xFFe8fb2a), Color(0xFF00ffa6))
        )

        val radius = (size.width / 2)

        rotate(155f) {
            var lineHeight: Float
            for (i in 0..230 step 6) {
                lineHeight = if (i % 12 == 0) {
                    16f
                } else {
                    10f
                }
                drawLines(i.toFloat(),radius, lineHeight)
            }
            if (animateFrac.value > 0f) {
                val progressVal = (230f * animateFrac.value).toInt()
                for (i in 0..progressVal step 6) {
                    lineHeight = if (i % 12 == 0) {
                        16f
                    } else {
                        10f
                    }
                    drawLinesProgress(i.toFloat(),radius, lineHeight, paint)
                }
            }
        }
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