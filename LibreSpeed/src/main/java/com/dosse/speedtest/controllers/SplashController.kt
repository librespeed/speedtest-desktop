package com.dosse.speedtest.controllers

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import java.io.IOException
import java.net.URL
import java.util.*

class SplashController : Initializable {

    @FXML
    private lateinit var root_splash : AnchorPane

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        val mainStarter = MainStarter()
        mainStarter.setOnEndListener(object : MainStarter.OnEndListener{
            override fun onEnded() {
                root_splash.scene.window.hide()
            }
        })
        mainStarter.start()
    }

    class MainStarter : Thread() {

        private lateinit var onEndListener: OnEndListener

        override fun run() {
            try {
                sleep(300)
                Platform.runLater {
                    var root : Parent? = null
                    try {
                        root = FXMLLoader.load<Parent>(Objects.requireNonNull(javaClass.getResource("/layouts/layout_main.fxml")))
                    } catch (_: IOException) {}
                    root!!.stylesheets.add(javaClass.getResource("/styles/styles.css")!!.toExternalForm())
                    val stage = Stage()
                    stage.icons.add(Image(javaClass.getResourceAsStream("/images/icon_app.png")))
                    stage.title = "LibreSpeed"
                    stage.scene = Scene(root, 430.0, 600.0)
                    stage.isResizable = false
                    stage.show()
                    onEndListener.onEnded()
                }
            } catch (_: InterruptedException) {

            }
        }


        fun setOnEndListener (onEndListener: OnEndListener) {
            this.onEndListener = onEndListener
        }
        interface OnEndListener {
            fun onEnded ()
        }
    }
}