import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextDecoration
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.dosse.speedtest.res.Res
import com.dosse.speedtest.res.icon_app
import com.dosse.speedtest.res.icon_app_tile
import components.SimpleButton
import core.Database
import core.Service
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition
import routes.Route
import routes.dialogs.BaseDialog
import routes.scenes.HistoryScene
import routes.scenes.HomeScene
import routes.scenes.SplashScene
import theme.ColorBox
import theme.Fonts
import theme.rippleConfiguration
import util.UpdateChecker
import util.Utils.openInBrowser
import java.awt.Desktop
import java.awt.Dimension

object App {

    val showLoading = mutableStateOf(false)
    val showAbout = mutableStateOf(false)

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    PreComposeApp {
        val navigator = rememberNavigator()
        MaterialTheme(
            typography = Fonts.getTypography()
        ) {
            CompositionLocalProvider(LocalRippleConfiguration provides rippleConfiguration) {
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

                BaseDialog(expanded = App.showLoading.value) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(Modifier.padding(20.dp), strokeCap = StrokeCap.Round)
                        Text(
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                            text = "Please wait ...",
                            color = ColorBox.text,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = Fonts.open_sans),
                        )
                    }
                }

                BaseDialog(
                    expanded = App.showAbout.value,
                    onDismissRequest = {
                        App.showAbout.value = false
                    }
                ) {
                    Column(modifier = Modifier.width(320.dp).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            modifier = Modifier.size(84.dp),
                            painter = painterResource(Res.drawable.icon_app_tile),
                            contentDescription = null
                        )
                        Text(
                            modifier = Modifier.padding(top = 12.dp),
                            text = "LibreSpeed",
                            color = ColorBox.text,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            modifier = Modifier.padding(top = 4.dp),
                            text = "Version ${UpdateChecker.currentVersion ?: "dev"}${UpdateChecker.buildDate?.let { " • built $it" } ?: ""}",
                            color = ColorBox.text.copy(0.6f),
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = Fonts.open_sans)
                        )
                        val linkColor = if (ColorBox.isNightTheme) Color(0xFF9E9EE8) else ColorBox.primary
                        Text(
                            modifier = Modifier.padding(top = 16.dp).pointerHoverIcon(PointerIcon.Hand).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                UpdateChecker.PROJECT_PAGE.openInBrowser()
                            },
                            text = "github.com/librespeed/speedtest-desktop",
                            color = linkColor,
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = Fonts.open_sans, textDecoration = TextDecoration.Underline)
                        )
                        Text(
                            modifier = Modifier.padding(top = 10.dp).pointerHoverIcon(PointerIcon.Hand).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                "https://librespeed.org".openInBrowser()
                            },
                            text = "librespeed.org",
                            color = linkColor,
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = Fonts.open_sans, textDecoration = TextDecoration.Underline)
                        )
                        SimpleButton(
                            modifier = Modifier.padding(top = 20.dp).width(140.dp),
                            text = "Close",
                            onClick = {
                                App.showAbout.value = false
                            }
                        )
                    }
                }
            }
        }
    }

}

fun main() = application {
    LaunchedEffect(Unit) {
        Database.initDB()
        Service.init()
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.APP_ABOUT)) {
                Desktop.getDesktop().setAboutHandler { App.showAbout.value = true }
            }
        } catch (_: Throwable) { }
    }
    Window(
        onCloseRequest = ::exitApplication,
        resizable = true,
        state = WindowState(width = 750.dp, height = 650.dp),
        title = "LibreSpeed"
    ) {
        window.minimumSize = Dimension(750,650)
        val icon = painterResource(Res.drawable.icon_app)
        val density = LocalDensity.current
        SideEffect {
            window.iconImage = icon.toAwtImage(density,LayoutDirection.Ltr, Size(128f,128f))
        }
        App()
    }
}
