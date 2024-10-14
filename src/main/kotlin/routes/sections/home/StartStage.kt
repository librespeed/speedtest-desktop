package routes.sections.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import components.AnimatedText
import components.StartButton
import core.Service
import dev.icerock.moko.mvvm.livedata.compose.observeAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import theme.ColorBox
import theme.Fonts
import util.NetUtils

@Composable
fun StartStage(onStartClicked: () -> Unit, onChooseServerClicked: () -> Unit) {

    val testPoint = Service.testPoint.observeAsState()
    var isReady by remember { mutableStateOf(false) }
    var networkAdapter by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val netInterface = NetUtils.getDefaultNetworkInterface()
            networkAdapter = if (netInterface != null) {
                "${netInterface.name} (${NetUtils.parseMacAddress(netInterface.hardwareAddress)})"
            } else {
                "Unknown"
            }
        }
        delay(2000)
        isReady = true
    }

    Column(modifier = Modifier.fillMaxSize(),verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(290.dp), contentAlignment = Alignment.Center) {
            this@Column.AnimatedVisibility(
                visible = isReady,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                StartButton(modifier = Modifier.size(290.dp)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clip(RoundedCornerShape(50)).clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onStartClicked.invoke()
                    })
            }
            this@Column.AnimatedVisibility(
                visible = !isReady,
                enter = fadeIn(),
                exit = fadeOut() + scaleOut(targetScale = 1.8f)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(80.dp),strokeCap = StrokeCap.Round)
            }
        }

        Row(
            modifier = Modifier.padding(top = 30.dp).width(390.dp).height(56.dp).clip(RoundedCornerShape(20.dp))
                .background(ColorBox.card)
                .clickable { onChooseServerClicked.invoke() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.padding(16.dp).size(24.dp),
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
                modifier = Modifier.padding(end = 16.dp).size(18.dp).rotate(-90f),
                painter = painterResource("icons/arrow-right.svg"),
                contentDescription = null,
                tint = ColorBox.text.copy(0.8f)
            )
        }

        Row(modifier = Modifier.padding(top = 20.dp),verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource("icons/network.svg"),
                contentDescription = null,
                tint = ColorBox.text.copy(0.6f)
            )
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = networkAdapter,
                color = ColorBox.text.copy(0.6f),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = Fonts.open_sans)
            )
        }

    }

}