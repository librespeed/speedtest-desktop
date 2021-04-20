package com.dosse.speedtest.core.serverSelector

import com.dosse.speedtest.core.config.SpeedtestConfig
import com.dosse.speedtest.core.ping.PingStream
import com.dosse.speedtest.widget.json.JSONArray
import com.dosse.speedtest.widget.json.JSONObject

abstract class ServerSelector(servers: Array<TestPoint>, timeout: Int) {
    private val servers = ArrayList<TestPoint>()
    private var selectedTestPoint: TestPoint? = null
    private var state = NOT_STARTED
    private val timeout: Int
    private var stopASAP = false
    fun addTestPoint(t: TestPoint?) {
        check(state == NOT_STARTED) { "Cannot add test points at this time" }
        if (t == null) return
        servers.add(t)
    }

    fun addTestPoint(t: com.dosse.speedtest.widget.json.JSONObject?) {
        check(state == NOT_STARTED) { "Cannot add test points at this time" }
        servers.add(TestPoint(t!!))
    }

    fun addTestPoints(a: com.dosse.speedtest.widget.json.JSONArray) {
        check(state == NOT_STARTED) { "Cannot add test points at this time" }
        for (i in 0 until a.length()) {
            try {
                servers.add(TestPoint(a.getJSONObject(i)))
            } catch (e: Exception) {
            }
        }
    }

    fun addTestPoints(servers: Array<TestPoint>) {
        check(state == NOT_STARTED) { "Cannot add test points at this time" }
        for (t in servers) addTestPoint(t)
    }

    fun getSelectedTestPoint(): TestPoint? {
        check(state == DONE) { "Test point hasn't been selected yet" }
        return selectedTestPoint
    }

    val testPoints: Array<TestPoint>
        get() = servers.toTypedArray()
    private val mutex = Any()
    private var tpPointer = 0
    private var activeStreams = 0
    private operator fun next() {
        if (stopASAP) return
        synchronized(mutex) {
            if (tpPointer >= servers.size) {
                if (activeStreams <= 0) {
                    selectedTestPoint = null
                    for (t in servers) {
                        if (t.ping == -1f) continue
                        if (selectedTestPoint == null || t.ping < selectedTestPoint!!.ping) selectedTestPoint = t
                    }
                    if (state == DONE) return
                    state = DONE
                    onServerSelected(selectedTestPoint)
                }
                return
            }
            val tp = servers[tpPointer++]
            val ps: PingStream = object : PingStream(
                tp.server!!,
                tp.pingURL!!,
                PINGS,
                SpeedtestConfig.ONERROR_FAIL,
                timeout,
                timeout,
                -1,
                -1,
                null
            ) {
                override fun onError(err: String?) {
                    tp.ping = (-1).toFloat()
                    synchronized(mutex) { activeStreams-- }
                    next()
                }

                override fun onPong(ns: Long): Boolean {
                    val p = ns / 1000000f
                    if (tp.ping == -1f || p < tp.ping) tp.ping = p
                    return if (stopASAP) false else p < SLOW_THRESHOLD
                }

                override fun onDone() {
                    synchronized(mutex) { activeStreams-- }
                    next()
                }
            }
            activeStreams++
        }
    }

    fun start() {
        check(state == NOT_STARTED) { "Already started" }
        state = WORKING
        for (t in servers) t.ping = (-1).toFloat()
        for (i in 0 until PARALLELISM) next()
    }

    fun stopASAP() {
        stopASAP = true
    }

    abstract fun onServerSelected(server: TestPoint?)

    companion object {
        private const val PARALLELISM = 6
        private const val NOT_STARTED = 0
        private const val WORKING = 1
        private const val DONE = 2
        private const val PINGS = 3
        private const val SLOW_THRESHOLD = 500
    }

    init {
        addTestPoints(servers)
        this.timeout = timeout
    }
}