package com.dosse.speedtest

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.util.*

class Main : Application() {

    fun main(args: Array<String>) {
        launch(*args)
    }

    override fun start(primaryStage: Stage) {
        val root = FXMLLoader.load<Parent>(Objects.requireNonNull(javaClass.getResource("/layouts/layout_splash.fxml")))
        primaryStage.icons.add(Image(javaClass.getResourceAsStream("/images/icon_app.png")))
        primaryStage.title = "LibreSpeed"
        primaryStage.scene = Scene(root, 430.0, 600.0)
        primaryStage.isResizable = false
        primaryStage.initStyle(StageStyle.UNDECORATED)
        primaryStage.show()
    }

}