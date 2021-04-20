package com.dosse.speedtest.controllers

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXProgressBar
import com.jfoenix.controls.JFXSpinner
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.VBox
import com.dosse.speedtest.Tools.Companion.fadeHide
import com.dosse.speedtest.Tools.Companion.fadeShow
import com.dosse.speedtest.core.SpeedTestHandler
import com.dosse.speedtest.core.SpeedTestHandler.OnServerSelectListener
import com.dosse.speedtest.core.Speedtest.SpeedtestHandler
import com.dosse.speedtest.core.serverSelector.TestPoint
import com.dosse.speedtest.widget.Gauge
import java.net.URL
import java.util.*

class MainController : Initializable {

    @FXML
    lateinit var root_main: AnchorPane
    @FXML
    lateinit var layout_select_server: VBox
    @FXML
    lateinit var layout_testing: VBox
    @FXML
    lateinit var layout_error: VBox
    @FXML
    lateinit var spinner_loading_servers: JFXSpinner
    @FXML
    lateinit var btn_select_server: JFXButton
    @FXML
    lateinit var btn_start_test: JFXButton
    @FXML
    lateinit var btn_new_test: JFXButton
    @FXML
    lateinit var btn_retry_error: JFXButton
    @FXML
    lateinit var btn_privacy: JFXButton
    @FXML
    lateinit var btn_copy_share: JFXButton
    @FXML
    lateinit var gauge_download: Gauge
    @FXML
    lateinit var gauge_upload: Gauge
    @FXML
    lateinit var progress_download: JFXProgressBar
    @FXML
    lateinit var progress_upload: JFXProgressBar
    @FXML
    lateinit var txt_server_name: Label
    @FXML
    lateinit var txt_ping: Label
    @FXML
    lateinit var txt_jitter: Label
    @FXML
    lateinit var txt_ip_info: Label

    private lateinit var speedTestHandler: SpeedTestHandler
    private var testPointMain: TestPoint? = null

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        defs()


        btn_new_test.onAction = EventHandler {
            speedTestHandler.stopTest()
            fadeShow(layout_select_server)
            fadeHide(layout_testing)
            fadeHide(layout_error)
            reset()
        }


        btn_privacy.onAction = EventHandler {
            val dialogShowPrivacy = DialogShowPrivacy(root_main)
            dialogShowPrivacy.setCancelable(false)
            dialogShowPrivacy.showDialog()
        }
    }

    private fun defs() {
        btn_select_server.isDisable = true
        btn_start_test.isDisable = true
        speedTestHandler = SpeedTestHandler(root_main)
        speedTestHandler.setOnServerSelectListener(object : OnServerSelectListener {
            override fun onServerSelected(testPoint: TestPoint?) {
                Platform.runLater {
                    fadeHide(spinner_loading_servers)
                    testPointMain = testPoint
                    btn_select_server.isDisable = false
                    btn_start_test.isDisable = false
                    txt_server_name.text = testPointMain!!.name
                    btn_select_server.text = testPointMain!!.name
                }
            }
        })
        speedTestHandler.startup()
        btn_select_server.onAction = EventHandler {
            val dialogSelectServer = DialogSelectServer(
                root_main,
                speedTestHandler.servers
            )
            dialogSelectServer.setCancelable(false)
            dialogSelectServer.setOnConfirmListener { testPoint ->
                dialogSelectServer.cancelDialog()
                testPointMain = testPoint
                txt_server_name.text = testPointMain!!.name
                btn_select_server.text = testPointMain!!.name
            }
            dialogSelectServer.showDialog()
        }
        btn_start_test.onAction = EventHandler {
            if (testPointMain == null) {
            } else {
                fadeHide(btn_copy_share)
                btn_copy_share.isManaged = false
                fadeShow(layout_testing)
                fadeHide(layout_select_server)
                speedTestHandler.startTest(testPointMain, object : SpeedtestHandler() {
                    override fun onDownloadUpdate(dl: Double, progress: Double) {
                        Platform.runLater {
                            progress_download.progress = progress
                            gauge_download.value = dl
                            btn_new_test.text = "Stop Test"
                        }
                    }

                    override fun onUploadUpdate(ul: Double, progress: Double) {
                        Platform.runLater {
                            progress_upload.progress = progress
                            gauge_upload.value = ul
                            btn_new_test.text = "Stop Test"
                        }
                    }

                    override fun onPingJitterUpdate(ping: Double, jitter: Double, progress: Double) {
                        Platform.runLater {
                            txt_ping.text = format(ping)
                            txt_jitter.text = format(jitter)
                            btn_new_test.text = "Stop Test"
                        }
                    }

                    override fun onIPInfoUpdate(ipInfo: String?) {
                        Platform.runLater {
                            txt_ip_info.text = ipInfo
                            btn_new_test.text = "Stop Test"
                        }
                    }

                    override fun onTestIDReceived(id: String?, shareURL: String?) {
                        btn_copy_share.isManaged = true
                        fadeShow(btn_copy_share)
                        btn_copy_share.onAction = EventHandler {
                            val clipboard = Clipboard.getSystemClipboard()
                            val content = ClipboardContent()
                            content.putString(shareURL)
                            clipboard.setContent(content)
                        }
                    }

                    override fun onEnd() {
                        Platform.runLater {
                            btn_new_test.text = "New Test"
                            speedTestHandler.stopTest()
                        }
                    }

                    override fun onCriticalFailure(err: String?) {
                        println("ERRRR :: $err")
                        speedTestHandler.stopTest()
                        Platform.runLater {
                            fadeShow(layout_error)
                            fadeHide(layout_testing)
                            fadeHide(layout_select_server)
                            reset()
                        }
                    }
                })
            }
        }
        btn_retry_error.onAction = EventHandler {
            fadeShow(layout_select_server)
            fadeHide(layout_testing)
            fadeHide(layout_error)
            reset()
        }
    }

    private fun reset() {
        txt_ping.text = "0.0"
        txt_jitter.text = "0.0"
        gauge_download.value = 0.0
        gauge_upload.value = 0.0
        progress_download.progress = 0.0
        progress_upload.progress = 0.0
        txt_ip_info.text = "IP Information"
        btn_new_test.text = "New Test"
    }

    private fun format(d: Double): String {
        if (d < 10) return String.format("%.2f", d)
        return if (d < 100) String.format("%.1f", d) else "" + Math.round(d)
    }
}