package core

import androidx.compose.ui.ExperimentalComposeUiApi
import com.dosse.speedtest.res.Res
import core.lib.LibreSpeed
import core.lib.LibreSpeed.ServerSelectedHandler
import core.lib.LibreSpeed.SpeedtestHandler
import core.lib.config.SpeedtestConfig
import core.lib.config.TelemetryConfig
import core.lib.serverSelector.TestPoint
import util.json.JSONArray
import util.json.JSONObject
import java.io.IOException
import java.net.URISyntaxException

class SpeedTestHandler {

    private var onServerSelectListener: ServerSelectedHandler? = null
    private var libreSpeed : LibreSpeed? = null

    @OptIn(ExperimentalComposeUiApi::class)
    suspend fun startup() : Boolean {
        libreSpeed = LibreSpeed()
        try {
            val telemetryConfig = TelemetryConfig(JSONObject(String(Res.readBytes("files/TelemetryConfig.json"), Charsets.UTF_8)))
            libreSpeed?.setTelemetryConfig(telemetryConfig)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            val speedTestConfig = SpeedtestConfig(JSONObject(String(Res.readBytes("files/SpeedtestConfig.json"), Charsets.UTF_8)))
            libreSpeed?.setSpeedtestConfig(speedTestConfig)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        libreSpeed!!.setSpeedtestConfig(SpeedtestConfig())
        return fetchServers()
    }

    fun startTest(testPoint: TestPoint?, speedTestHandler: SpeedtestHandler?) {
        libreSpeed!!.setSelectedServer(testPoint)
        libreSpeed!!.start(speedTestHandler!!)
    }

    fun stopTest() {
        libreSpeed!!.abort()
    }

    @OptIn(ExperimentalComposeUiApi::class)
    private suspend fun fetchServers() : Boolean {
        val data: String
        try {
            data = String(Res.readBytes("files/ServerList.json"), Charsets.UTF_8)
        } catch (_: Exception) {
            return false
        }
        if (data.startsWith("\"") || data.startsWith("'")) { //fetch server list from URL
            if (!libreSpeed!!.loadServerList(data.subSequence(1, data.length - 1).toString())) {
                return false
            }
        } else {
            val testPoints = JSONArray(data)
            libreSpeed!!.addTestPoints(testPoints)
        }
        libreSpeed!!.selectServer(onServerSelectListener)
        return true
    }

    val servers: ArrayList<TestPoint>
        get() = ArrayList(listOf(*libreSpeed!!.testPoints))

    fun setOnServerSelectListener(onServerSelectListener: ServerSelectedHandler?) {
        this.onServerSelectListener = onServerSelectListener
    }

}