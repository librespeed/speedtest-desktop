package routes.scenes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import components.LinearChart
import core.Service
import core.Service.toValidString
import moe.tlaster.precompose.navigation.Navigator
import theme.ColorBox

@Composable
fun ResultScene(navigator: Navigator) {

    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier.fillMaxSize().background(ColorBox.primaryDark),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(64.dp).background(color = ColorBox.primaryDark.copy(0.5f)),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "Test Result",
                color = ColorBox.text,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Row(
            Modifier.padding(top = 25.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.padding(start = 35.dp).weight(1f)) {
                Text(
                    text = "Ping",
                    color = ColorBox.text.copy(0.5f),
                    style = MaterialTheme.typography.bodySmall
                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Service.ping.value,
                        color = ColorBox.text,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = " ms",
                        color = ColorBox.text.copy(0.5f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            LinearChart(
                modifier = Modifier.padding(start = 10.dp, end = 35.dp).height(52.dp).weight(1f),
                color = Color(0xFFEF9595),
                data = Service.pingChart
            )
        }
        Row(
            Modifier.padding(top = 20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.padding(start = 35.dp).weight(1f)) {
                Text(
                    text = "Jitter",
                    color = ColorBox.text.copy(0.5f),
                    style = MaterialTheme.typography.bodySmall
                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Service.jitter.value,
                        color = ColorBox.text,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = " ms",
                        color = ColorBox.text.copy(0.5f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            LinearChart(
                modifier = Modifier.padding(start = 10.dp, end = 35.dp).height(52.dp).weight(1f),
                color = Color(0xFFEFB495),
                data = Service.jitterChart
            )
        }
        Row(
            Modifier.padding(top = 20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.padding(start = 35.dp).weight(1f)) {
                Text(
                    text = "Download",
                    color = ColorBox.text.copy(0.5f),
                    style = MaterialTheme.typography.bodySmall
                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Service.download.value.toValidString(),
                        color = ColorBox.text,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = " ${Service.unitSetting.value}",
                        color = ColorBox.text.copy(0.5f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            LinearChart(
                modifier = Modifier.padding(start = 10.dp, end = 35.dp).height(52.dp).weight(1f),
                color = Color(0xFFEFD595),
                data = Service.downloadChart
            )
        }
        Row(
            Modifier.padding(top = 20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.padding(start = 35.dp).weight(1f)) {
                Text(
                    text = "Upload",
                    color = ColorBox.text.copy(0.5f),
                    style = MaterialTheme.typography.bodySmall
                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Service.upload.value.toValidString(),
                        color = ColorBox.text,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = " ${Service.unitSetting.value}",
                        color = ColorBox.text.copy(0.5f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            LinearChart(
                modifier = Modifier.padding(start = 10.dp, end = 35.dp).height(52.dp).weight(1f),
                color = Color(0xFFEBEF95),
                data = Service.uploadChart
            )
        }
        Text(
            modifier = Modifier.padding(top = 35.dp, start = 25.dp, end = 25.dp).fillMaxWidth(),
            text = Service.ipInfo.value,
            color = ColorBox.text.copy(0.8f),
            style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center)
        )
        Text(
            modifier = Modifier.padding(top = 16.dp, start = 25.dp, end = 25.dp).fillMaxWidth(),
            text = Service.testPoint.value?.name.toString(),
            color = ColorBox.text.copy(0.8f),
            style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center)
        )
        Box(
            Modifier
                .padding(top = 25.dp, start = 35.dp, end = 35.dp).height(48.dp)
                .fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .border(
                    2.dp,
                    brush = Brush.linearGradient(listOf(ColorBox.primary, Color(0xFF7E60AA))),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable {
                    clipboardManager.setText(AnnotatedString(Service.testIDShare.value.toString()))
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Copy result url",
                color = ColorBox.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Box(
            Modifier
                .padding(top = 16.dp)
                .height(48.dp)
                .fillMaxWidth(fraction = 0.7f).clip(RoundedCornerShape(16.dp))
                .border(
                    2.dp,
                    brush = Brush.linearGradient(listOf(Color(0xFF7E60AA),ColorBox.primary)),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable {
                    Service.reset()
                    navigator.popBackStack()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "New Test",
                color = ColorBox.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

}