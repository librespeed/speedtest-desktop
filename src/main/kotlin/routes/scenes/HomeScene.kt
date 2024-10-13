package routes.scenes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import components.CustomBottomSheet
import components.Tab
import components.TabSwitcher
import components.rememberBottomSheetState
import core.Service
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.Navigator
import routes.Route
import routes.sections.HomeBottomSheet
import routes.sections.home.Appbar
import routes.sections.home.ResultStage
import routes.sections.home.StartStage
import routes.sections.home.TestStage

enum class Stage {
    Start,
    Test,
    Result
}

var selectedStage = mutableStateOf(Stage.Start)

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
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Appbar(onHistoryClicked = {
                    navigator.navigate(Route.HISTORY)
                })
                TabSwitcher(
                    modifier = Modifier.fillMaxSize(),
                    selectedTab = selectedStage.value.name,
                    tabs = arrayOf(
                        Tab(name = Stage.Start.name) {
                            StartStage(
                                onStartClicked = {
                                    Service.startTesting()
                                    selectedStage.value = Stage.Test
                                },
                                onChooseServerClicked = {
                                    scope.launch {
                                        bottomSheetState.open()
                                    }
                                }
                            )
                        },
                        Tab(name = Stage.Test.name) {
                            TestStage(
                                onCancel = {
                                    Service.reset()
                                    selectedStage.value = Stage.Start
                                },
                                goToResult = {
                                    selectedStage.value = Stage.Result
                                }
                            )
                        },
                        Tab(name = Stage.Result.name) {
                            ResultStage(
                                newTestClicked = {
                                    Service.reset()
                                    selectedStage.value = Stage.Start
                                }
                            )
                        }
                    )
                )
            }
        }
    )


}