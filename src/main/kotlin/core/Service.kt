package core

import androidx.compose.runtime.snapshots.SnapshotStateList
import core.lib.LibreSpeed
import core.lib.serverSelector.TestPoint
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import util.Utils.roundPlace
import util.Utils.toMegabyte
import util.Utils.validate

object Service {

    private lateinit var speedTestHandler: SpeedTestHandler

    const val UNIT_MBYTE = "MB/s"
    const val UNIT_MBIT = "mbps"

    val unitSetting = MutableLiveData(UNIT_MBIT)

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

    var goToResult : () -> Unit = {}
    var onError : (String?) -> Unit = {}

    fun init () {
        speedTestHandler = SpeedTestHandler()
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

    fun startFetchServers (finished : () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            speedTestHandler.startup()
            speedTestHandler.setOnServerSelectListener(object : SpeedTestHandler.OnServerSelectListener{
                override fun onError() {

                }
                override fun onServerSelected(testPoint: TestPoint?) {
                    this@Service.testPoint.value = testPoint
                    CoroutineScope(Dispatchers.Main).launch {
                        finished.invoke()
                    }
                }
            })
        }
    }

    fun startTesting () {
        if (running.value) {
            speedTestHandler.stopTest()
            running.value = false
        } else {
            running.value = true
            CoroutineScope(Dispatchers.IO).launch {
                speedTestHandler.startTest(this@Service.testPoint.value,object : LibreSpeed.SpeedtestHandler() {
                    override fun onDownloadUpdate(dl: Double, progress: Double) {
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
                    override fun onUploadUpdate(ul: Double, progress: Double) {
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
                    override fun onPingJitterUpdate(ping: Double, jitter: Double, progress: Double) {
                        currentStep.value = "PING"
                        progressPing.value = progress
                        unit.value = "ms"
                        currentCalValuePr.value = ((ping.toFloat() * 100f) / 500f) / 100f
                        currentCalValue.value = ping
                        this@Service.ping.value = ping.roundPlace(1).toString()
                        this@Service.jitter.value = jitter.roundPlace(1).toString()
                        pingChart.add(ping)
                        jitterChart.add(jitter)
                    }
                    override fun onIPInfoUpdate(ipInfo: String?) {
                        this@Service.ipInfo.value = ipInfo.toString()
                    }
                    override fun onTestIDReceived(id: String?, shareURL: String?) {
                        testIDShare.value = shareURL
                    }
                    override fun onEnd() {
                        currentStep.value = "ENDED"
                        if (!running.value) reset() else goToResult.invoke()
                        running.value = false
                    }
                    override fun onCriticalFailure(err: String?) {
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