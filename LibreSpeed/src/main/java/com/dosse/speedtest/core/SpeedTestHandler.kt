package com.dosse.speedtest.core

import com.google.gson.Gson
import javafx.scene.layout.AnchorPane
import com.dosse.speedtest.core.Speedtest.ServerSelectedHandler
import com.dosse.speedtest.core.Speedtest.SpeedtestHandler
import com.dosse.speedtest.core.config.SpeedtestConfig
import com.dosse.speedtest.core.config.TelemetryConfig
import com.dosse.speedtest.core.serverSelector.TestPoint
import java.io.IOException
import java.net.URISyntaxException
import java.util.*

class SpeedTestHandler(private val anchorPane: AnchorPane) {
    private val arrayList = ArrayList<TestPoint>()
    private var onServerSelectListener: OnServerSelectListener? = null
    private var speedtest: Speedtest? = null
    fun startup() {
        speedtest = Speedtest()
        val gson = Gson()
        try {
            val telemetryConfig = gson.fromJson(
                readFileFromResources("/configs/TelemetryConfig.json"),
                TelemetryConfig::class.java
            )
            speedtest!!.setTelemetryConfig(telemetryConfig)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            val speedtestConfig = gson.fromJson(
                readFileFromResources("/configs/SpeedtestConfig.json"),
                SpeedtestConfig::class.java
            )
            speedtest!!.setSpeedtestConfig(speedtestConfig)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        speedtest!!.setSpeedtestConfig(SpeedtestConfig())
        fetchServers()
    }

    fun startTest(testPoint: TestPoint?, speedTestHandler: SpeedtestHandler?) {
        speedtest!!.setSelectedServer(testPoint)
        speedtest!!.start(speedTestHandler!!)
    }

    fun stopTest() {
        speedtest!!.abort()
    }

    private fun fetchServers() {
        var data = ""
        try {
            data = readFileFromResources("/configs/ServerList.json")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (data.startsWith("\"") || data.startsWith("'")) { //fetch server list from URL
            if (!speedtest!!.loadServerList(data.subSequence(1, data.length - 1).toString())) {
                try {
                    throw Exception("Failed to load server list")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            val gson = Gson()
            val testPoints = gson.fromJson(
                data,
                Array<TestPoint>::class.java
            )
            speedtest!!.addTestPoints(testPoints)
        }
        speedtest!!.selectServer(object : ServerSelectedHandler() {
            override fun onServerSelected(server: TestPoint?) {
                onServerSelectListener!!.onServerSelected(server)
            }
        })
    }

    val servers: ArrayList<TestPoint>
        get() = ArrayList(Arrays.asList(*speedtest!!.testPoints))

    @Throws(URISyntaxException::class, IOException::class)
    fun readFileFromResources(filename: String?): String {
        val inputStream = anchorPane.javaClass.getResourceAsStream(filename)
        val s = Scanner(inputStream).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }

    fun setOnServerSelectListener(onServerSelectListener: OnServerSelectListener?) {
        this.onServerSelectListener = onServerSelectListener
    }

    interface OnServerSelectListener {
        fun onServerSelected(testPoint: TestPoint?)
    }
}