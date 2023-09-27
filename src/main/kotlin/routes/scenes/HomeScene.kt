package routes.scenes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import components.CustomBottomSheet
import components.rememberBottomSheetState
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.Navigator
import routes.sections.HomeBottomSheet
import routes.sections.HomeContent

@Composable
fun HomeScene(navigator: Navigator) {

    val bottomSheetState = rememberBottomSheetState()
    val scope = rememberCoroutineScope()

    CustomBottomSheet(
        scope = scope,
        state = bottomSheetState,
        sheetContent = {
            HomeBottomSheet(
                closeClicked = {
                    scope.launch {
                        bottomSheetState.close()
                    }
                }
            )
        },
        content = {
            HomeContent(
                navigator = navigator,
                onServerSelectClick = {
                    scope.launch {
                        bottomSheetState.open()
                    }
                }
            )
        }
    )

}