package routes.scenes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import components.CustomBottomSheet
import components.Tab
import components.TabSwitcher
import components.rememberBottomSheetState
import core.Service
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.Navigator
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

@Composable
fun HomeScene(navigator: Navigator) {

    val bottomSheetState = rememberBottomSheetState()
    val scope = rememberCoroutineScope()

    var selectedStage by remember { mutableStateOf(Stage.Start) }

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
                Appbar()
                TabSwitcher(
                    modifier = Modifier.fillMaxSize(),
                    selectedTab = selectedStage.name,
                    tabs = arrayOf(
                        Tab(name = Stage.Start.name) {
                            StartStage(
                                onStartClicked = {
                                    Service.startTesting()
                                    selectedStage = Stage.Test
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
                                    selectedStage = Stage.Start
                                },
                                goToResult = {
                                    selectedStage = Stage.Result
                                }
                            )
                        },
                        Tab(name = Stage.Result.name) {
                            ResultStage(
                                newTestClicked = {
                                    Service.reset()
                                    selectedStage = Stage.Start
                                }
                            )
                        }
                    )
                )
            }
        }
    )


}