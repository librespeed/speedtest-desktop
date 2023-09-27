package core.lib.worker

import core.lib.base.Connection
import core.lib.base.Utils
import core.lib.config.SpeedtestConfig
import core.lib.config.TelemetryConfig
import core.lib.download.DownloadStream
import core.lib.getIP.GetIP
import core.lib.log.Logger
import core.lib.ping.PingStream
import core.lib.serverSelector.TestPoint
import core.lib.telemetry.Telemetry
import core.lib.upload.UploadStream
import util.json.JSONObject
import java.util.*

abstract class SpeedtestWorker(
    private val backend: TestPoint,
    config: SpeedtestConfig?,
    telemetryConfig: TelemetryConfig?
) :
    Thread() {
    private val config: SpeedtestConfig
    private val telemetryConfig: TelemetryConfig
    private var stopASAP = false
    private var dl = -1.0
    private var ul = -1.0
    private var ping = -1.0
    private var jitter = -1.0
    private var ipIsp: String? = ""
    private val log = Logger()
    override fun run() {
        log.l("Test started")
        try {
            for (t in config.getTest_order().toCharArray()) {
                if (stopASAP) break
                if (t == '_') Utils.sleep(1000)
                if (t == 'I') iP
                if (t == 'D') dlTest()
                if (t == 'U') ulTest()
                if (t == 'P') pingTest()
            }
        } catch (t: Throwable) {
            onCriticalFailure(t.toString())
        }
        try {
            sendTelemetry()
        } catch (t: Throwable) {
        }
        onEnd()
    }

    private var getIPCalled = false
    private val iP: Unit
        private get() {
            getIPCalled = if (getIPCalled) return else true
            val start = System.currentTimeMillis()
            var c: Connection? = null
            c = try {
                Connection(backend.server!!, config.ping_connectTimeout, config.ping_soTimeout, -1, -1)
            } catch (t: Throwable) {
                if (config.getErrorHandlingMode() == SpeedtestConfig.ONERROR_FAIL) {
                    abort()
                    onCriticalFailure(t.toString())
                }
                return
            }
            val g: GetIP = object : GetIP(c!!, backend.getIpURL!!, config.getIP_isp, config.getGetIP_distance()) {
                override fun onDataReceived(data: String?) {
                    var data = data
                    ipIsp = data
                    try {
                        data = JSONObject(data!!)["processedString"].toString()
                    } catch (t: Throwable) {
                    }
                    log.l("GetIP: " + data + " (took " + (System.currentTimeMillis() - start) + "ms)")
                    onIPInfoUpdate(data)
                }

                override fun onError(err: String?) {
                    log.l("GetIP: FAILED (took " + (System.currentTimeMillis() - start) + "ms)")
                    abort()
                    onCriticalFailure(err)
                }
            }
            while (g.isAlive) Utils.sleep(0, 100)
        }
    private var dlCalled = false
    private fun dlTest() {
        dlCalled = if (dlCalled) return else true
        val start = System.currentTimeMillis()
        onDownloadUpdate(0.0, 0.0)
        val streams = arrayOfNulls<DownloadStream>(config.getDl_parallelStreams())
        for (i in streams.indices) {
            streams[i] = object : DownloadStream(
                backend.server!!,
                backend.dlURL!!,
                config.getDl_ckSize(),
                config.getErrorHandlingMode(),
                config.dl_connectTimeout,
                config.dl_soTimeout,
                config.dl_recvBuffer,
                config.dl_sendBuffer,
                log
            ) {
                override fun onError(err: String?) {
                    log.l("Download: FAILED (took " + (System.currentTimeMillis() - start) + "ms)")
                    abort()
                    onCriticalFailure(err)
                }
            }
            Utils.sleep(config.getDl_streamDelay().toLong())
        }
        var graceTimeDone = false
        var startT = System.currentTimeMillis()
        var bonusT: Long = 0
        while (true) {
            val t = (System.currentTimeMillis() - startT).toDouble()
            if (!graceTimeDone && t >= config.getDl_graceTime() * 1000) {
                graceTimeDone = true
                for (d in streams) d!!.resetDownloadCounter()
                startT = System.currentTimeMillis()
                continue
            }
            if (stopASAP || t + bonusT >= config.getTime_dl_max() * 1000) {
                for (d in streams) d!!.stopASAP()
                for (d in streams) d!!.join()
                break
            }
            if (graceTimeDone) {
                var totDownloaded: Long = 0
                for (d in streams) totDownloaded += d!!.totalDownloaded
                var speed: Double = totDownloaded / ((if (t < 100) 100 else t).toLong() / 1000.0)
                if (config.time_auto) {
                    val b = 2.5 * speed / 100000.0
                    bonusT += if (b > 200) 200 else b.toLong()
                }
                val progress = (t + bonusT) / (config.getTime_dl_max() * 1000).toDouble()
                speed =
                    speed * 8 * config.getOverheadCompensationFactor() / if (config.useMebibits) 1048576.0 else 1000000.0
                dl = speed
                onDownloadUpdate(dl, (if (progress > 1) 1 else progress).toDouble())
            }
            Utils.sleep(100)
        }
        if (stopASAP) return
        log.l("Download: " + dl + " (took " + (System.currentTimeMillis() - start) + "ms)")
        onDownloadUpdate(dl, 1.0)
    }

    private var ulCalled = false
    private fun ulTest() {
        ulCalled = if (ulCalled) return else true
        val start = System.currentTimeMillis()
        onUploadUpdate(0.0, 0.0)
        val streams = arrayOfNulls<UploadStream>(config.getUl_parallelStreams())
        for (i in streams.indices) {
            streams[i] = object : UploadStream(
                backend.server!!,
                backend.ulURL!!,
                config.getUl_ckSize(),
                config.getErrorHandlingMode(),
                config.ul_connectTimeout,
                config.ul_soTimeout,
                config.ul_recvBuffer,
                config.ul_sendBuffer,
                log
            ) {
                override fun onError(err: String?) {
                    log.l("Upload: FAILED (took " + (System.currentTimeMillis() - start) + "ms)")
                    abort()
                    onCriticalFailure(err)
                }
            }
            Utils.sleep(config.getUl_streamDelay().toLong())
        }
        var graceTimeDone = false
        var startT = System.currentTimeMillis()
        var bonusT: Long = 0
        while (true) {
            val t = (System.currentTimeMillis() - startT).toDouble()
            if (!graceTimeDone && t >= config.getUl_graceTime() * 1000) {
                graceTimeDone = true
                for (u in streams) u!!.resetUploadCounter()
                startT = System.currentTimeMillis()
                continue
            }
            if (stopASAP || t + bonusT >= config.getTime_ul_max() * 1000) {
                for (u in streams) u!!.stopASAP()
                for (u in streams) u!!.join()
                break
            }
            if (graceTimeDone) {
                var totUploaded: Long = 0
                for (u in streams) totUploaded += u!!.totalUploaded
                var speed: Double = totUploaded / ((if (t < 100) 100 else t).toLong() / 1000.0)
                if (config.time_auto) {
                    val b = 2.5 * speed / 100000.0
                    bonusT += if (b > 200) 200 else b.toLong()
                }
                val progress = (t + bonusT) / (config.getTime_ul_max() * 1000).toDouble()
                speed =
                    speed * 8 * config.getOverheadCompensationFactor() / if (config.useMebibits) 1048576.0 else 1000000.0
                ul = speed
                onUploadUpdate(ul, (if (progress > 1) 1 else progress).toDouble())
            }
            Utils.sleep(100)
        }
        if (stopASAP) return
        log.l("Upload: " + ul + " (took " + (System.currentTimeMillis() - start) + "ms)")
        onUploadUpdate(ul, 1.0)
    }

    private var pingCalled = false
    private fun pingTest() {
        pingCalled = if (pingCalled) return else true
        val start = System.currentTimeMillis()
        onPingJitterUpdate(0.0, 0.0, 0.0)
        val ps: PingStream = object : PingStream(
            backend.server!!,
            backend.pingURL!!,
            config.getCount_ping(),
            config.getErrorHandlingMode(),
            config.ping_connectTimeout,
            config.ping_soTimeout,
            config.ping_recvBuffer,
            config.ping_sendBuffer,
            log
        ) {
            private var minPing = Double.MAX_VALUE
            private var prevPing = -1.0
            private var counter = 0
            override fun onError(err: String?) {
                log.l("Ping: FAILED (took " + (System.currentTimeMillis() - start) + "ms)")
                abort()
                onCriticalFailure(err)
            }

            override fun onPong(ns: Long): Boolean {
                counter++
                val ms = ns / 1000000.0
                if (ms < minPing) minPing = ms
                ping = minPing
                jitter = if (prevPing == -1.0) {
                    0.0
                } else {
                    val j = Math.abs(ms - prevPing)
                    if (j > jitter) jitter * 0.3 + j * 0.7 else jitter * 0.8 + j * 0.2
                }
                prevPing = ms
                val progress = counter / config.getCount_ping().toDouble()
                onPingJitterUpdate(ping, jitter, (if (progress > 1) 1 else progress).toDouble())
                return !stopASAP
            }

            override fun onDone() {}
        }
        ps.join()
        if (stopASAP) return
        log.l("Ping: " + ping + " " + jitter + " (took " + (System.currentTimeMillis() - start) + "ms)")
        onPingJitterUpdate(ping, jitter, 1.0)
    }

    private fun sendTelemetry() {
        if (telemetryConfig.telemetryLevel == TelemetryConfig.LEVEL_DISABLED) return
        if (stopASAP && telemetryConfig.telemetryLevel == TelemetryConfig.LEVEL_BASIC) return
        try {
            val c = Connection(telemetryConfig.server!!, -1, -1, -1, -1)
            val t: Telemetry = object : Telemetry(
                c,
                telemetryConfig.path!!,
                telemetryConfig.telemetryLevel,
                ipIsp!!,
                config.telemetry_extra,
                if (dl == -1.0) "" else String.format(Locale.ENGLISH, "%.2f", dl),
                if (ul == -1.0) "" else String.format(Locale.ENGLISH, "%.2f", ul),
                if (ping == -1.0) "" else String.format(Locale.ENGLISH, "%.2f", ping),
                if (jitter == -1.0) "" else String.format(Locale.ENGLISH, "%.2f", jitter),
                log.getLog()
            ) {
                override fun onDataReceived(data: String?) {
                    if (data!!.startsWith("id")) {
                        onTestIDReceived(data.split(" ").toTypedArray()[1])
                    }
                }

                override fun onError(err: String?) {
                    System.err.println("Telemetry error: $err")
                }
            }
            t.join()
        } catch (t: Throwable) {
            System.err.println("Failed to send telemetry: $t")
            t.printStackTrace(System.err)
        }
    }

    fun abort() {
        if (stopASAP) return
        log.l("Manually aborted")
        stopASAP = true
    }

    abstract fun onDownloadUpdate(dl: Double, progress: Double)
    abstract fun onUploadUpdate(ul: Double, progress: Double)
    abstract fun onPingJitterUpdate(ping: Double, jitter: Double, progress: Double)
    abstract fun onIPInfoUpdate(ipInfo: String?)
    abstract fun onTestIDReceived(id: String?)
    abstract fun onEnd()
    abstract fun onCriticalFailure(err: String?)

    init {
        this.config = config ?: SpeedtestConfig()
        this.telemetryConfig = telemetryConfig ?: TelemetryConfig()
        start()
    }
}