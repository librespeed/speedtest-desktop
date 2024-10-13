package routes.sections.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import components.AnimatedText
import components.SimpleButton
import components.SparkUp
import components.SpeedMeterView
import core.Service
import core.Service.toValidString
import dev.icerock.moko.mvvm.livedata.compose.observeAsState
import theme.ColorBox
import util.Utils.roundPlace
import java.util.*

@Composable
fun TestStage(onCancel : () -> Unit,goToResult : () -> Unit) {

    val currentStep = Service.currentStep.observeAsState()

    val calcValue = Service.currentCalValue.observeAsState()
    val calcValuePercentage = Service.currentCalValuePr.observeAsState()
    val unitSetting = Service.unitSetting.observeAsState()
    val unit = Service.unit.observeAsState()

    val ipInfo = Service.ipInfo.observeAsState()

    val ping = Service.ping.observeAsState()
    val jitter = Service.jitter.observeAsState()
    val download = Service.download.observeAsState()
    val upload = Service.upload.observeAsState()

    val progressPing = Service.progressPing.observeAsState()
    val progressDownload = Service.progressDownload.observeAsState()
    val progressUpload = Service.progressUpload.observeAsState()

    var enablecancelation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        Service.goToResult = {
            goToResult.invoke()
        }
        Service.onError = {
            onCancel.invoke()
        }
        Service.onEnableAbort = {
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    enablecancelation = true
                }
            },2000)
        }
    }

    Box(Modifier.fillMaxSize()) {
        val chartData = when (currentStep.value.lowercase()) {
            "download" -> Service.downloadChart
            "upload" -> Service.uploadChart
            "ping" -> Service.pingChart
            "jitter" -> Service.jitterChart
            else -> listOf()
        }
        val progress = when (currentStep.value.lowercase()) {
            "download" -> progressDownload.value
            "upload" -> progressUpload.value
            "ping" -> progressPing.value
            "jitter" -> progressPing.value
            else -> 0.0
        }
        SparkUp(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(90.dp), data = chartData.toList())
        Column(modifier = Modifier.fillMaxSize(),verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                SpeedMeterView(speed = calcValuePercentage.value, modifier = Modifier.size(316.dp), progress = progress.toFloat())
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    AnimatedText(
                        modifier = Modifier.padding(top = 76.dp),
                        text = currentStep.value,
                        color = ColorBox.text.copy(0.3f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                        text = if (currentStep.value == "DOWNLOAD" || currentStep.value == "UPLOAD") calcValue.value.toValidString() else calcValue.value.roundPlace(
                            1
                        ).toString(),
                        color = ColorBox.text.copy(0.9f),
                        style = MaterialTheme.typography.displayMedium
                    )
                    AnimatedText(
                        modifier = Modifier.padding(bottom = 36.dp),
                        text = unit.value,
                        color = ColorBox.text.copy(0.4f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    AnimatedText(
                        modifier = Modifier.padding(start = 30.dp, end = 30.dp, bottom = 25.dp),
                        text = ipInfo.value,
                        color = ColorBox.text.copy(0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource("icons/server-change.svg"),
                            contentDescription = null,
                            tint = ColorBox.text.copy(0.7f)
                        )
                        Text(
                            modifier = Modifier.padding(start = 16.dp),
                            text = Service.testPoint.value?.name.toString(),
                            color = ColorBox.text.copy(0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            SimpleButton(
                modifier = Modifier.padding(top = 16.dp).width(120.dp),
                onClick = {
                    onCancel.invoke()
                },
                text = "Stop Test",
                backgroundColor = ColorBox.error,
                textColor = Color.White
            )
            Row(modifier = Modifier.padding(top = 32.dp).height(120.dp),verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.width(180.dp),horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "Ping",
                        color = ColorBox.text.copy(0.7f),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        modifier = Modifier.padding(top = 12.dp),
                        text = ping.value,
                        color = ColorBox.text,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        modifier = Modifier.padding(top = 12.dp),
                        text = "ms",
                        color = ColorBox.text.copy(0.7f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(ColorBox.text.copy(0.7f)))
                Column(modifier = Modifier.width(180.dp),horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "Jitter",
                        color = ColorBox.text.copy(0.7f),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        modifier = Modifier.padding(top = 12.dp),
                        text = jitter.value,
                        color = ColorBox.text,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        modifier = Modifier.padding(top = 12.dp),
                        text = "ms",
                        color = ColorBox.text.copy(0.7f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(ColorBox.text.copy(0.7f)))
                Column(modifier = Modifier.width(180.dp),horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "Download",
                        color = ColorBox.text.copy(0.7f),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        modifier = Modifier.padding(top = 12.dp),
                        text = download.value.toValidString(),
                        color = ColorBox.text,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        modifier = Modifier.padding(top = 12.dp),
                        text = unitSetting.value,
                        color = ColorBox.text.copy(0.7f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(ColorBox.text.copy(0.7f)))
                Column(modifier = Modifier.width(180.dp),horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "Upload",
                        color = ColorBox.text.copy(0.7f),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        modifier = Modifier.padding(top = 12.dp),
                        text = upload.value.toValidString(),
                        color = ColorBox.text,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        modifier = Modifier.padding(top = 12.dp),
                        text = unitSetting.value,
                        color = ColorBox.text.copy(0.7f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }

}