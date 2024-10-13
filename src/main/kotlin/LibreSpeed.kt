import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import core.Database
import core.Service
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition
import routes.Route
import routes.scenes.HistoryScene
import routes.scenes.HomeScene
import routes.scenes.SplashScene
import theme.AppRippleTheme
import theme.ColorBox
import theme.Fonts
import java.awt.Dimension

@Composable
fun App() {
    Service.init()

    PreComposeApp {
        val navigator = rememberNavigator()

        MaterialTheme(
            typography = Fonts.getTypography()
        ) {
            CompositionLocalProvider(LocalRippleTheme provides AppRippleTheme) {
                NavHost(
                    modifier = Modifier.background(ColorBox.primaryDark),
                    navigator = navigator,
                    navTransition = NavTransition(),
                    initialRoute = Route.SPLASH,
                ) {
                    scene(
                        route = Route.SPLASH,
                        navTransition = NavTransition(),
                    ) {
                        SplashScene(navigator)
                    }
                    scene(
                        route = Route.HOME,
                        navTransition = NavTransition()
                    ) {
                        HomeScene(navigator)
                    }
                    scene(
                        route = Route.HISTORY,
                        navTransition = NavTransition()
                    ) {
                        HistoryScene(navigator)
                    }
                }
            }
        }
    }

}

fun main() = application {
    LaunchedEffect(Unit) {
        Database.initDB()
    }
    Window(
        onCloseRequest = ::exitApplication,
        resizable = true,
        state = WindowState(width = 990.dp, height = 650.dp),
        title = "LibreSpeed"
    ) {
        window.minimumSize = Dimension(990,650)
        val icon = painterResource("icons/icon_app.svg")
        val density = LocalDensity.current
        SideEffect {
            window.iconImage = icon.toAwtImage(density,LayoutDirection.Ltr, Size(128f,128f))
        }
        App()
    }
}
