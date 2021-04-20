package com.dosse.speedtest

import javafx.animation.FadeTransition
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.util.Duration

class Tools {

    companion object {

        fun fadeShow(pane: Node) {
            val fadeIN = FadeTransition()
            if (pane.opacity == 1.0) {
                pane.isVisible = true
            } else {
                fadeIN.fromValue = 0.0
                fadeIN.toValue = 1.0
                fadeIN.duration = Duration.millis(300.0)
                fadeIN.node = pane
                fadeIN.playFromStart()
                fadeIN.onFinished = EventHandler { pane.isVisible = true }
            }
        }

        fun fadeHide(pane: Node) {
            val fadeOut = FadeTransition()
            if (pane.opacity == 0.0) {
                pane.isVisible = false
            } else {
                fadeOut.fromValue = 1.0
                fadeOut.toValue = 0.0
                fadeOut.duration = Duration.millis(300.0)
                fadeOut.node = pane
                fadeOut.playFromStart()
                fadeOut.onFinished = EventHandler { pane.isVisible = false }
            }
        }
    }

}