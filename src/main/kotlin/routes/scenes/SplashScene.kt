package routes.scenes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import core.Service
import moe.tlaster.precompose.navigation.Navigator
import routes.Route
import theme.ColorBox

@Composable
fun SplashScene(navigator: Navigator) {

    LaunchedEffect(Unit) {
        Service.startFetchServers {
            navigator.navigate(Route.HOME)
        }
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
            Column(modifier = Modifier.padding(top = 32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    Modifier.size(18.dp),
                    strokeCap = StrokeCap.Round,
                    strokeWidth = 2.dp,
                    color = ColorBox.COLOR_TEXT_NIGHT
                )
                Spacer(Modifier.padding(6.dp))
                Text(
                    text = "Finding best servers ...",
                    fontSize = 13.sp,
                    color = ColorBox.COLOR_TEXT_NIGHT.copy(0.8f)
                )
            }
        }
    }


}