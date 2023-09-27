package routes.sections

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import components.AnimatedText
import components.MyIconButton
import components.ProgressView
import components.menu.UnitMenu
import core.Service
import core.Service.toValidString
import dev.icerock.moko.mvvm.livedata.compose.observeAsState
import moe.tlaster.precompose.navigation.Navigator
import routes.Route
import routes.dialogs.BaseDialog
import routes.dialogs.DialogPrivacy
import theme.ColorBox
import util.Utils.roundPlace

@Composable
fun HomeContent(
    navigator: Navigator,
    onServerSelectClick: () -> Unit
) {

    val testPoint = Service.testPoint.observeAsState()

    val isRunning = Service.running.observeAsState()
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

    var showUnitMenu by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Service.goToResult = {
            navigator.navigate(Route.RESULT)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().background(ColorBox.primaryDark),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    modifier = Modifier.padding(start = 12.dp).size(44.dp),
                    painter = painterResource("icons/icon_app.svg"),
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp).weight(1f),
                    text = "LibreSpeed",
                    color = ColorBox.text,
                    style = MaterialTheme.typography.titleMedium
                )
                MyIconButton(
                    padding = PaddingValues(end = 8.dp),
                    icon = "icons/security.svg",
                    onClick = {
                        showPrivacyDialog = true
                    }
                )
                Row(
                    Modifier.padding(end = 8.dp).height(44.dp).clip(RoundedCornerShape(50))
                        .background(ColorBox.text.copy(0.1f)).clickable {
                        showUnitMenu = true
                    }.padding(start = 10.dp, end = 10.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource("icons/speed.svg"),
                        contentDescription = null,
                        tint = ColorBox.text.copy(0.8f)
                    )
                    Spacer(Modifier.padding(5.dp))
                    AnimatedText(
                        text = unitSetting.value,
                        color = ColorBox.text.copy(0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Box(Modifier.padding(top = 30.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                ProgressView(modifier = Modifier.size(280.dp), progress = calcValuePercentage.value)
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedText(
                        modifier = Modifier.padding(top = 40.dp),
                        text = currentStep.value,
                        color = ColorBox.text.copy(0.3f),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                        text = if (currentStep.value == "DOWNLOAD" || currentStep.value == "UPLOAD") calcValue.value.toValidString() else calcValue.value.roundPlace(
                            1
                        ).toString(),
                        color = ColorBox.text.copy(0.9f),
                        style = MaterialTheme.typography.displayLarge
                    )
                    AnimatedText(
                        modifier = Modifier.padding(bottom = 20.dp),
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
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(
                    Modifier.fillMaxWidth().weight(1f).padding(start = 20.dp, end = 10.dp).clip(RoundedCornerShape(12.dp)).drawBehind {
                        drawRect(
                            color = ColorBox.primary.copy(0.3f),
                            size = Size(size.width * progressPing.value.toFloat(), size.height)
                        )
                    },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = "Ping",
                        color = ColorBox.text.copy(0.5f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                        text = ping.value,
                        color = ColorBox.text,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Box(Modifier.width(2.dp).height(40.dp).background(ColorBox.text.copy(0.1f)))
                Column(
                    Modifier.fillMaxWidth().weight(1f).padding(start = 10.dp, end = 20.dp).clip(RoundedCornerShape(12.dp)).drawBehind {
                        drawRect(
                            color = ColorBox.primary.copy(0.3f),
                            size = Size(size.width * progressPing.value.toFloat(), size.height)
                        )
                    },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = "Jitter",
                        color = ColorBox.text.copy(0.5f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                        text = jitter.value,
                        color = ColorBox.text,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            Row(Modifier.padding(top = 14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(
                    Modifier.fillMaxWidth().weight(1f).padding(start = 20.dp, end = 10.dp).clip(RoundedCornerShape(12.dp)).drawBehind {
                        drawRect(
                            color = ColorBox.primary.copy(0.3f),
                            size = Size(size.width * progressDownload.value.toFloat(), size.height)
                        )
                    },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = "Download",
                        color = ColorBox.text.copy(0.5f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                        text = download.value.toValidString(),
                        color = ColorBox.text,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Box(Modifier.width(2.dp).height(40.dp).background(ColorBox.text.copy(0.1f)))
                Column(
                    Modifier.fillMaxWidth().weight(1f).padding(start = 10.dp, end = 20.dp).clip(RoundedCornerShape(12.dp)).drawBehind {
                        drawRect(
                            color = ColorBox.primary.copy(0.3f),
                            size = Size(size.width * progressUpload.value.toFloat(), size.height)
                        )
                    },
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = "Upload",
                        color = ColorBox.text.copy(0.5f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                        text = upload.value.toValidString(),
                        color = ColorBox.text,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

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
                        Service.startTesting()
                    },
                contentAlignment = Alignment.Center
            ) {
                val buttonText = when (currentStep.value) {
                    "DOWNLOAD", "UPLOAD", "PING" -> "Stop Test"
                    "" -> "Start Test"
                    else -> "Retry Test"
                }
                Text(
                    text = buttonText,
                    color = ColorBox.text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth(0.8f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)).background(ColorBox.card)
                .alpha(if (!isRunning.value) 1f else 0.4f)
                .clickable(enabled = !isRunning.value) {
                    onServerSelectClick.invoke()
                }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.padding(12.dp).size(24.dp),
                painter = painterResource("icons/server-change.svg"),
                contentDescription = null,
                tint = ColorBox.text.copy(0.7f)
            )
            AnimatedText(
                modifier = Modifier.padding(end = 12.dp).weight(1f),
                text = if (testPoint.value == null) "Choice an server to start" else testPoint.value!!.name!!,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = ColorBox.text.copy(0.8f),
                style = MaterialTheme.typography.bodySmall
            )
            Icon(
                modifier = Modifier.padding(end = 12.dp).size(18.dp).rotate(-90f),
                painter = painterResource("icons/arrow-right.svg"),
                contentDescription = null,
                tint = ColorBox.text.copy(0.8f)
            )
        }
    }

    UnitMenu(
        show = showUnitMenu,
        onDismissRequest = {
            showUnitMenu = false
        }
    )

    BaseDialog(
        expanded = showPrivacyDialog,
        onDismissRequest = {
            showPrivacyDialog = false
        },
        backgroundColor = ColorBox.primaryDark
    ) {
        DialogPrivacy {
            showPrivacyDialog = false
        }
    }

}