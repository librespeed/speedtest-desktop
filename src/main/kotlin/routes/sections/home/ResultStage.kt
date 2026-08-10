package routes.sections.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import components.SimpleButton
import components.SparkUp
import core.Service
import core.Service.toValidString
import dev.icerock.moko.mvvm.livedata.compose.observeAsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import theme.ColorBox
import theme.Fonts
import util.Utils.openInBrowser
import java.awt.datatransfer.StringSelection

@Composable
fun ResultStage(newTestClicked: () -> Unit) {

    val clipboardManager = LocalClipboard.current
    val unitSetting = Service.unitSetting.observeAsState()
    val shareUrl = Service.testIDShare.observeAsState()
    val scope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

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
            shareUrl.value?.let { url ->
                SimpleButton(
                    modifier = Modifier.padding(end = 6.dp).width(160.dp),
                    text = "Open Result URL",
                    onClick = {
                        url.openInBrowser()
                    }
                )
                SimpleButton(
                    modifier = Modifier.padding(end = 6.dp).width(160.dp),
                    text = if (copied) "Copied ✓" else "Copy Result URL",
                    onClick = {
                        scope.launch {
                            clipboardManager.setClipEntry(clipEntry = ClipEntry(StringSelection(url)))
                            copied = true
                        }
                    }
                )
            }
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
    Box(modifier = modifier.height(112.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(0.1f))) {
        //chart lives in its own band at the bottom so it never overlaps the texts
        SparkUp(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(42.dp),
            data = chartData,
            drawGradient = true,
            color = color,
            vPadding = 8f
        )
        Column(modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = Fonts.open_sans),
                color = ColorBox.text.copy(0.7f)
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = Fonts.open_sans),
                    color = ColorBox.text
                )
                Text(
                    modifier = Modifier.padding(start = 6.dp, bottom = 5.dp),
                    text = suffix,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = Fonts.open_sans),
                    color = ColorBox.text.copy(0.6f)
                )
            }
        }
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