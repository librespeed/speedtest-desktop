package components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun TabSwitcher (modifier: Modifier,selectedTab : String,vararg tabs : Tab) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        tabs.forEach { tab ->
            AnimatedVisibility(
                visible = selectedTab == tab.name,
                enter = tab.enterTransition,
                exit = tab.exitTransition
            ) {
                tab.content(this@Box)
            }
        }
    }
}

data class Tab(
    var name : String,
    var enterTransition: EnterTransition = fadeIn() + expandVertically(),
    var exitTransition: ExitTransition = fadeOut() + shrinkVertically(),
    var content : @Composable BoxScope.() -> Unit
)