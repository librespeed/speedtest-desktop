package routes.scenes

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import core.Service
import moe.tlaster.precompose.navigation.Navigator
import routes.Route
import theme.ColorBox
import theme.Fonts

@Composable
fun SplashScene(navigator: Navigator) {

    var serverSelectionLog by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        Service.onServerSelected = {
            navigator.navigate(Route.HOME)
        }
        Service.startFetchServers(result = { _, log ->
            serverSelectionLog = log
        })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().background(ColorBox.primary),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(120.dp),
                painter = painterResource("icons/icon_app.svg"),
                contentDescription = null
            )
            Column(modifier = Modifier.padding(top = 32.dp).animateContentSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    Modifier.size(18.dp),
                    strokeCap = StrokeCap.Round,
                    strokeWidth = 2.dp,
                    color = ColorBox.COLOR_TEXT_NIGHT
                )
                Spacer(Modifier.padding(8.dp))
                Text(
                    text = serverSelectionLog,
                    color = ColorBox.COLOR_TEXT_NIGHT.copy(0.8f),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = Fonts.open_sans, textAlign = TextAlign.Center)
                )
            }
        }
    }


}