package routes.sections.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import components.SparkUp
import core.Service
import core.Service.toValidString
import dev.icerock.moko.mvvm.livedata.compose.observeAsState
import theme.ColorBox

@Composable
fun ResultStage(newTestClicked : () -> Unit) {

    val clipboardManager = LocalClipboardManager.current
    val unitSetting = Service.unitSetting.observeAsState()

    Column(modifier = Modifier.widthIn(max = 800.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Column(
            modifier = Modifier.fillMaxWidth().height(64.dp).background(color = ColorBox.primaryDark.copy(0.5f)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = Service.ipInfo.value,
                color = ColorBox.text.copy(0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                modifier = Modifier.padding(top = 12.dp),
                text = Service.testPoint.value?.name.toString(),
                color = ColorBox.text.copy(0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Row(
            Modifier.padding(top = 25.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.padding(start = 35.dp, end = 35.dp)) {
                Text(
                    text = "Ping",
                    color = ColorBox.text.copy(0.5f),
                    style = MaterialTheme.typography.bodySmall
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
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
            SparkUp(
                modifier = Modifier.weight(1f).padding(start = 10.dp, end = 35.dp).height(52.dp).weight(1f),
                data = Service.pingChart
            )
        }
        Row(
            Modifier.padding(top = 20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.padding(start = 35.dp, end = 35.dp)) {
                Text(
                    text = "Jitter",
                    color = ColorBox.text.copy(0.5f),
                    style = MaterialTheme.typography.bodySmall
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
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
            SparkUp(
                modifier = Modifier.padding(start = 10.dp, end = 35.dp).height(52.dp).weight(1f),
                data = Service.jitterChart
            )
        }
        Row(
            Modifier.padding(top = 20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.padding(start = 35.dp, end = 35.dp)) {
                Text(
                    text = "Download",
                    color = ColorBox.text.copy(0.5f),
                    style = MaterialTheme.typography.bodySmall
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Service.download.value.toValidString(),
                        color = ColorBox.text,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = " ${unitSetting.value}",
                        color = ColorBox.text.copy(0.5f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            SparkUp(
                modifier = Modifier.padding(start = 10.dp, end = 35.dp).height(52.dp).weight(1f),
                data = Service.downloadChart
            )
        }
        Row(
            Modifier.padding(top = 20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.padding(start = 35.dp, end = 35.dp)) {
                Text(
                    text = "Upload",
                    color = ColorBox.text.copy(0.5f),
                    style = MaterialTheme.typography.bodySmall
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Service.upload.value.toValidString(),
                        color = ColorBox.text,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = " ${unitSetting.value}",
                        color = ColorBox.text.copy(0.5f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            SparkUp(
                modifier = Modifier.padding(start = 10.dp, end = 35.dp).height(52.dp).weight(1f),
                data = Service.uploadChart
            )
        }

        Button(modifier = Modifier.width(220.dp).padding(top = 36.dp),onClick = {
            clipboardManager.setText(AnnotatedString(Service.testIDShare.value.toString()))
        }) {
            Text("Copy result url")
        }
        Button(modifier = Modifier.width(220.dp).padding(top = 12.dp),onClick = {
            newTestClicked.invoke()
        }) {
            Text("New Test")
        }

    }
}