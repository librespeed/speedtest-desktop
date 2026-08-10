package core.lib

import core.lib.config.SpeedtestConfig
import core.lib.config.TelemetryConfig
import core.lib.serverSelector.ServerSelector
import core.lib.serverSelector.TestPoint
import core.lib.worker.SpeedtestWorker
import util.json.JSONArray
import util.json.JSONException
import util.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class LibreSpeed {

    private val servers = ArrayList<TestPoint>()
    private var selectedServer: TestPoint? = null
    private var config = SpeedtestConfig()
    private var telemetryConfig = TelemetryConfig()
    private var state = 0 //0=configs, 1=test points, 2=server selection, 3=ready, 4=testing, 5=finished
    private val mutex = Any()
    private var originalExtra: String? = ""

    fun setSpeedtestConfig(c: SpeedtestConfig) {
        synchronized(mutex) {
            check(state == 0) { "Cannot change config at this moment" }
            config = c.clone()
            val extra = config.telemetry_extra
            if (extra.isNotEmpty()) originalExtra = extra
        }
    }

    fun setTelemetryConfig(c: TelemetryConfig) {
        synchronized(mutex) {
            check(state == 0) { "Cannot change config at this moment" }
            telemetryConfig = c.clone()
        }
    }

    fun addTestPoint(t: TestPoint) {
        synchronized(mutex) {
            if (state == 0) state = 1
            check(state <= 1) { "Cannot add test points at this moment" }
            servers.add(t)
        }
    }

    fun addTestPoints(s: Array<TestPoint>) {
        synchronized(mutex) { for (t in s) addTestPoint(t) }
    }

    fun addTestPoint(json: JSONObject?) {
        synchronized(mutex) { addTestPoint(TestPoint().fromJson(json!!)) }
    }

    fun addTestPoints(json: JSONArray) {
        synchronized(mutex) {
            for (i in 0 until json.length()) try {
                addTestPoint(json.getJSONObject(i))
            } catch (_: JSONException) { }
        }
    }

    private object ServerListLoader {
        private const val FETCH_TIMEOUT = 5000
        private fun read(url: String): String? {
            return try {
                val conn = URL(url).openConnection()
                conn.connectTimeout = FETCH_TIMEOUT
                conn.readTimeout = FETCH_TIMEOUT
                BufferedReader(InputStreamReader(conn.getInputStream())).use { br ->
                    val s = StringBuilder()
                    while (true) {
                        s.append(br.readLine() ?: break)
                    }
                    s.toString()
                }
            } catch (t: Throwable) {
                null
            }
        }

        fun loadServerList(url: String): Array<TestPoint>? {
            return try {
                var s: String?
                if (url.startsWith("//")) {
                    s = read("https:$url")
                    if (s == null) s = read("http:$url")
                } else s = read(url)
                if (s == null) throw Exception("Failed")
                val a = JSONArray(s)
                val ret = ArrayList<TestPoint>()
                for (i in 0 until a.length()) {
                    ret.add(TestPoint().fromJson(a.getJSONObject(i)))
                }
                ret.toTypedArray()
            } catch (t: Throwable) {
                null
            }
        }
    }

    fun loadServerList(url: String): Boolean {
        //the fetch can block for seconds; it must not hold the facade mutex
        synchronized(mutex) {
            if (state == 0) state = 1
            check(state <= 1) { "Cannot add test points at this moment" }
        }
        val pts = ServerListLoader.loadServerList(url) ?: return false
        addTestPoints(pts)
        return true
    }

    val testPoints: Array<TestPoint>
        get() {
            synchronized(mutex) { return servers.toTypedArray() }
        }
    private var ss: ServerSelector? = null
    fun selectServer(callback: ServerSelectedHandler?) {
        synchronized(mutex) {
            check(state != 0) { "No test points added" }
            check(state != 2) { "Server selection is in progress" }
            check(state <= 2) { "Server already selected" }
            state = 2
            ss = object : ServerSelector(testPoints, config.ping_connectTimeout) {
                override fun onServerSelected(server: TestPoint?) {
                    selectedServer = server
                    synchronized(mutex) { state = if (server != null) 3 else 1 }
                    callback?.onServerSelected(server)
                }
            }
            ss!!.start()
        }
    }

    fun setSelectedServer(t: TestPoint?) {
        synchronized(mutex) {
            check(state != 2) { "Server selection is in progress" }
            requireNotNull(t) { "t is null" }
            selectedServer = t
            state = 3
        }
    }

    @Volatile private var st: SpeedtestWorker? = null
    fun start(callback: SpeedtestHandler) {
        //abort() only flags the worker; wait for the previous one so two tests never
        //overlap. Clear the field first: the callbacks below ignore anything that is
        //no longer the current worker, and an aborted one usually dies during this
        //very wait -- its final onEnd would otherwise still pass that check and
        //navigate to a result, save a bogus row and invert the running flag.
        val previous = st
        st = null
        previous?.let { old -> try { old.join(3000) } catch (_: InterruptedException) { } }
        synchronized(mutex) {
            check(state >= 3) { "Server hasn't been selected yet" }
            check(state != 4) { "Test already running" }
            state = 4
            try {
                val extra = JSONObject()
                if (originalExtra != null && originalExtra!!.isNotEmpty()) extra.put("extra", originalExtra)
                extra.put("server", selectedServer!!.name)
                config.telemetry_extra = extra.toString()
            } catch (_: Throwable) { }
            st = object : SpeedtestWorker(selectedServer!!, config, telemetryConfig) {
                //a worker replaced by a newer test run must not reach the callback anymore
                override fun onDownloadUpdate(dl: Double, progress: Double) {
                    if (st !== this) return
                    callback.onDownloadUpdate(dl, progress)
                }

                override fun onUploadUpdate(ul: Double, progress: Double) {
                    if (st !== this) return
                    callback.onUploadUpdate(ul, progress)
                }

                override fun onPingJitterUpdate(ping: Double, jitter: Double, progress: Double) {
                    if (st !== this) return
                    callback.onPingJitterUpdate(ping, jitter, progress)
                }

                override fun onIPInfoUpdate(ipInfo: String?) {
                    if (st !== this) return
                    callback.onIPInfoUpdate(ipInfo)
                }

                override fun onTestIDReceived(id: String?, shareURLTemplate: String?) {
                    if (st !== this) return
                    var shareURL = shareURLTemplate
                    //the template is built from server-supplied URLs; a format call would choke on their '%' bytes
                    if (shareURL != null && id != null) shareURL = shareURL.replace("%s", id)
                    callback.onTestIDReceived(id, shareURL)
                }

                override fun onEnd() {
                    synchronized(mutex) {
                        if (st !== this) return
                        this@LibreSpeed.state = 5
                    }
                    callback.onEnd()
                }

                override fun onCriticalFailure(err: String?) {
                    synchronized(mutex) {
                        if (st !== this) return
                        this@LibreSpeed.state = 5
                    }
                    callback.onCriticalFailure(err)
                }
            }
        }
    }

    fun abort() {
        synchronized(mutex) {
            if (state == 2) ss!!.stopASAP()
            if (state == 4) {
                st!!.abort()
                st!!.interrupt() //wake blocking sleeps so the worker notices stopASAP promptly
            }
            state = 5
        }
    }

    abstract class ServerSelectedHandler {
        abstract fun onServerSelected(server: TestPoint?)
    }

    abstract class SpeedtestHandler {
        abstract fun onDownloadUpdate(dl: Double, progress: Double)
        abstract fun onUploadUpdate(ul: Double, progress: Double)
        abstract fun onPingJitterUpdate(ping: Double, jitter: Double, progress: Double)
        abstract fun onIPInfoUpdate(ipInfo: String?)
        abstract fun onTestIDReceived(id: String?, shareURL: String?)
        abstract fun onEnd()
        abstract fun onCriticalFailure(err: String?)
    }
}