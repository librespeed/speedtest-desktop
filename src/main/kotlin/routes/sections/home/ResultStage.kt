package routes.sections.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import components.SimpleButton
import components.calcCons
import components.calcPoints
import components.drawSparkLine
import core.Service
import core.Service.toValidString
import dev.icerock.moko.mvvm.livedata.compose.observeAsState
import org.jetbrains.skia.Point
import theme.ColorBox
import theme.Fonts

@Composable
fun ResultStage(newTestClicked: () -> Unit) {

    val clipboardManager = LocalClipboardManager.current
    val unitSetting = Service.unitSetting.observeAsState()

    Column(
        modifier = Modifier.widthIn(max = 720.dp).fillMaxSize().background(ColorBox.primaryDark),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            Modifier.padding(top = 28.dp, start = 16.dp, end = 16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ResultItem(
                modifier = Modifier.weight(1f).padding(end = 6.dp),
                color = Color(0xFF227c9d),
                title = "Ping",
                value = Service.ping.value,
                suffix = "ms",
                Service.pingChart
            )
            ResultItem(
                modifier = Modifier.weight(1f).padding(start = 6.dp),
                color = Color(0xFF17c3b2),
                title = "Jitter",
                value = Service.jitter.value,
                suffix = "ms",
                Service.jitterChart
            )
        }
        Row(
            Modifier.padding(top = 12.dp, start = 16.dp, end = 16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ResultItem(
                modifier = Modifier.weight(1f).padding(end = 6.dp),
                color = Color(0xFFffcb77),
                title = "Download",
                value = Service.download.value.toValidString(),
                suffix = unitSetting.value,
                Service.downloadChart
            )
            ResultItem(
                modifier = Modifier.weight(1f).padding(start = 6.dp),
                color = Color(0xFFfe6d73),
                title = "Upload",
                value = Service.upload.value.toValidString(),
                suffix = unitSetting.value,
                Service.uploadChart
            )
        }
        InfoItem(
            modifier = Modifier.padding(top = 32.dp, bottom = 8.dp).fillMaxWidth().padding(start = 32.dp, end = 32.dp),
            title = "Your IP",
            value = Service.ipInfo.value
        )
        HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(start = 40.dp, end = 40.dp), color = ColorBox.text.copy(0.2f))
        InfoItem(
            modifier = Modifier.padding(top = 8.dp, bottom = 5.dp).fillMaxWidth().padding(start = 32.dp, end = 32.dp),
            title = "Test Point",
            value = Service.testPoint.value?.name.toString()
        )
        HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(start = 40.dp, end = 40.dp), color = ColorBox.text.copy(0.2f))
        InfoItem(
            modifier = Modifier.padding(top = 8.dp).fillMaxWidth().padding(start = 32.dp, end = 32.dp),
            title = "Network Adapter",
            value = Service.networkAdapter.value
        )
        Row(modifier = Modifier.padding(top = 32.dp)) {
            SimpleButton(
                modifier = Modifier.padding(end = 6.dp).width(160.dp),
                text = "Copy Result URL",
                onClick = {
                    clipboardManager.setText(AnnotatedString(Service.testIDShare.value.toString()))
                }
            )
            SimpleButton(
                modifier = Modifier.padding(start = 6.dp).width(160.dp),
                text = "New Test",
                backgroundColor = ColorBox.primary,
                textColor = Color.White,
                onClick = {
                    newTestClicked.invoke()
                }
            )
        }
    }
}

@Composable
private fun ResultItem(
    modifier: Modifier,
    color: Color,
    title: String,
    value: String,
    suffix: String,
    chartData: List<Double>
) {

    val textMeasurer = rememberTextMeasurer()

    val titleText =
        textMeasurer.measure(title, style = MaterialTheme.typography.bodySmall.copy(fontFamily = Fonts.open_sans))
    val valueText =
        textMeasurer.measure(value, style = MaterialTheme.typography.headlineMedium.copy(fontFamily = Fonts.open_sans))
    val suffixText =
        textMeasurer.measure(suffix, style = MaterialTheme.typography.labelSmall.copy(fontFamily = Fonts.open_sans))

    val points = remember { ArrayList<Point>() }
    val conPoint1 = remember { ArrayList<Point>() }
    val conPoint2 = remember { ArrayList<Point>() }

    val path = remember { Path() }
    var canvasSize = remember { Size.Zero }

    LaunchedEffect(chartData.toList()) {
        if (chartData.isNotEmpty()) {
            calcPoints(canvasSize, chartData, points, vPadding = 16f)
            calcCons(points, conPoint1, conPoint2)
        }
    }

    Canvas(
        modifier = modifier.onSizeChanged {
            canvasSize = Size(it.width.toFloat(), it.height.toFloat())
            if (chartData.isNotEmpty()) {
                calcPoints(canvasSize, chartData, points, vPadding = 16f)
                calcCons(points, conPoint1, conPoint2)
            }
        }.height((12f + titleText.size.height + 4f + valueText.size.height + 12f).dp).clip(RoundedCornerShape(12.dp))
            .background(color.copy(0.1f))
    ) {
        drawSparkLine(
            path = path,
            points = points,
            conPoint1 = conPoint1,
            conPoint2 = conPoint2,
            color = color,
            drawGradient = false
        )
        //title text
        drawText(
            textLayoutResult = titleText,
            color = ColorBox.text.copy(0.7f),
            topLeft = Offset(16f.dp.toPx(), 12f.dp.toPx()),
        )
        //value text
        drawText(
            textLayoutResult = valueText,
            color = ColorBox.text,
            topLeft = Offset(16f.dp.toPx(), (12f + titleText.size.height + 4f).dp.toPx()),
        )
        //suffix text
        drawText(
            textLayoutResult = suffixText,
            color = ColorBox.text.copy(0.6f),
            topLeft = Offset(
                (16f + valueText.size.width + 6f).dp.toPx(),
                (12f + titleText.size.height - suffixText.size.height + valueText.size.height).dp.toPx()
            ),
        )
    }
}

@Composable
private fun InfoItem(
    modifier: Modifier,
    title: String,
    value: String
) {
    val textMeasurer = rememberTextMeasurer()
    val titleText =
        textMeasurer.measure(title, style = MaterialTheme.typography.labelSmall.copy(fontFamily = Fonts.open_sans))
    val valueText =
        textMeasurer.measure(value, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = Fonts.open_sans))

    Canvas(
        modifier = modifier.height((titleText.size.height + valueText.size.height).dp)
    ) {
        drawText(
            textLayoutResult = titleText,
            color = ColorBox.text.copy(0.5f),
            topLeft = Offset(12f.dp.toPx(), 0f)
        )
        drawText(
            textLayoutResult = valueText,
            color = ColorBox.text,
            Offset(12f.dp.toPx(), (titleText.size.height).dp.toPx())
        )
    }
}