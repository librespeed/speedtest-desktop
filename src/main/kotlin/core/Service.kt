package core

import androidx.compose.runtime.snapshots.SnapshotStateList
import core.lib.LibreSpeed
import core.lib.serverSelector.TestPoint
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import kotlinx.coroutines.*
import util.NetUtils
import javax.swing.SwingUtilities
import util.Utils.roundPlace
import util.Utils.toMegabyte
import util.Utils.validate

object Service {

    private lateinit var speedTestHandler: SpeedTestHandler

    const val UNIT_MBYTE = "MB/s"
    const val UNIT_MBIT = "mbps"

    val unitSetting = MutableLiveData(UNIT_MBIT)
    val networkAdapter = MutableLiveData("")

    val currentStep = MutableLiveData("")
    val currentCalValue = MutableLiveData(0.0)
    val currentCalValuePr = MutableLiveData(0f)
    val unit = MutableLiveData("")

    val ipInfo = MutableLiveData("")
    val testIDShare = MutableLiveData<String?>(null)

    val ping = MutableLiveData("00.0")
    val jitter = MutableLiveData("00.0")
    val download = MutableLiveData(0.0)
    val upload = MutableLiveData(0.0)

    val progressPing = MutableLiveData(0.0)
    val progressDownload = MutableLiveData(0.0)
    val progressUpload = MutableLiveData(0.0)

    val pingChart = SnapshotStateList<Double>()
    val jitterChart = SnapshotStateList<Double>()
    val downloadChart = SnapshotStateList<Double>()
    val uploadChart = SnapshotStateList<Double>()

    var testPoint = MutableLiveData<TestPoint?>(null)
    var running = MutableLiveData(false)
    val serversVersion = MutableLiveData(0)

    var goToResult : () -> Unit = {}
    var onError : (String?) -> Unit = {}
    var onEnableAbort : () -> Unit = {}
    var onServerSelected : () -> Unit = {}

    //moko LiveData is not thread-safe; engine callbacks arrive on worker threads and must reach it on the EDT
    private fun onUi(block: () -> Unit) {
        if (SwingUtilities.isEventDispatchThread()) block() else SwingUtilities.invokeLater(block)
    }

    fun init () {
        speedTestHandler = SpeedTestHandler()
        speedTestHandler.setOnServerSelectListener(object : LibreSpeed.ServerSelectedHandler() {
            override fun onServerSelected(server: TestPoint?) = onUi {
                this@Service.testPoint.value = server
                serversVersion.value = serversVersion.value + 1
                onServerSelected.invoke()
            }
        })
    }

    fun serverList () : ArrayList<TestPoint> {
        return speedTestHandler.servers
    }

    fun reset () {
        speedTestHandler.stopTest()
        running.value = false
        pingChart.clear()
        jitterChart.clear()
        downloadChart.clear()
        uploadChart.clear()
        currentStep.value = ""
        currentCalValue.value = 0.0
        currentCalValuePr.value = 0f
        unit.value = ""
        ipInfo.value = ""
        testIDShare.value = null
        ping.value = "00.0"
        jitter.value = "00.0"
        download.value = 0.0
        upload.value = 0.0
        progressPing.value = 0.0
        progressDownload.value = 0.0
        progressUpload.value = 0.0
    }

    suspend fun startFetchServers (result : (Boolean,String) -> Unit) {
        withContext(Dispatchers.IO) {
            while (true) {
                result.invoke(false,"Finding best servers ...")
                val serversSelectionResult = speedTestHandler.startup()
                if (serversSelectionResult) {
                    result.invoke(true,"Finding best servers ...")
                    break
                } else {
                    startCountdown(10) {
                        result.invoke(false,"Server selection failed !\nretrying in $it sec")
                    }
                }
            }
        }
    }
    private suspend fun startCountdown(seconds: Int,callback : (Int) -> Unit) {
        for (i in seconds downTo 1) {
            delay(1000)
            callback.invoke(i)
        }
        delay(1000)
    }

    fun startTesting () {
        if (running.value) {
            speedTestHandler.stopTest()
            running.value = false
        } else {
            running.value = true
            CoroutineScope(Dispatchers.IO).launch {
                //adapter detection shells out to slow system tools; never let it delay the test itself
                launch {
                    val netInterface = NetUtils.getDefaultNetworkInterface()
                    val description = if (netInterface != null) NetUtils.describeInterface(netInterface) else "Unknown"
                    onUi { networkAdapter.value = description }
                }
                speedTestHandler.startTest(this@Service.testPoint.value,object : LibreSpeed.SpeedtestHandler() {
                    override fun onDownloadUpdate(dl: Double, progress: Double) = onUi {
                        currentStep.value = "DOWNLOAD"
                        progressDownload.value = progress
                        if (unitSetting.value == UNIT_MBIT) {
                            unit.value = "mbps"
                        } else {
                            unit.value = "MB/s"
                        }
                        currentCalValuePr.value = (((dl.toFloat() * 100f) / 200f) / 100f).validate()
                        currentCalValue.value = dl
                        this@Service.download.value = dl
                        downloadChart.add(dl)
                    }
                    override fun onUploadUpdate(ul: Double, progress: Double) = onUi {
                        currentStep.value = "UPLOAD"
                        progressUpload.value = progress
                        if (unitSetting.value == UNIT_MBIT) {
                            unit.value = "mbps"
                        } else {
                            unit.value = "MB/s"
                        }
                        currentCalValuePr.value = (((ul.toFloat() * 100f) / 200f) / 100f).validate()
                        currentCalValue.value = ul
                        this@Service.upload.value = ul
                        uploadChart.add(ul)
                    }
                    override fun onPingJitterUpdate(ping: Double, jitter: Double, progress: Double) = onUi {
                        currentStep.value = "PING"
                        progressPing.value = progress
                        unit.value = "ms"
                        currentCalValuePr.value = ((ping.toFloat() * 100f) / 500f) / 100f
                        currentCalValue.value = ping
                        this@Service.ping.value = ping.roundPlace(1).toString()
                        this@Service.jitter.value = jitter.roundPlace(1).toString()
                        pingChart.add(ping)
                        jitterChart.add(jitter)
                        onEnableAbort.invoke()
                    }
                    override fun onIPInfoUpdate(ipInfo: String?) = onUi {
                        this@Service.ipInfo.value = ipInfo.toString()
                    }
                    override fun onTestIDReceived(id: String?, shareURL: String?) = onUi {
                        testIDShare.value = shareURL
                    }
                    override fun onEnd() = onUi {
                        currentStep.value = "ENDED"
                        if (!running.value) reset() else {
                            goToResult.invoke()
                            val entry = ModelHistory(
                                netAdapter = networkAdapter.value,
                                ping = ping.value.toDouble(),
                                jitter = jitter.value.toDouble(),
                                download = download.value,
                                upload = upload.value,
                                ispInfo = ipInfo.value,
                                testPoint = testPoint.value?.name.toString(),
                                date = System.currentTimeMillis(),
                                shareUrl = testIDShare.value
                            )
                            //the insert goes off the UI thread; the result screen is already showing
                            CoroutineScope(Dispatchers.IO).launch { Database.saveHistory(entry) }
                        }
                        running.value = false
                    }
                    override fun onCriticalFailure(err: String?) = onUi {
                        onError.invoke(err)
                        currentStep.value = "FAILED"
                        running.value = false
                    }
                })
            }
        }
    }

    fun Double.toValidString () : String {
        return if (this == 0.0) {
            "00.0"
        } else {
            if (unitSetting.value == UNIT_MBIT) {
                this.roundPlace(1).toString()
            } else {
                this.toMegabyte().roundPlace(1).toString()
            }
        }
    }

}