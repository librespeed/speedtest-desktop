package com.dosse.speedtest.core.ping

import com.dosse.speedtest.core.base.Connection
import com.dosse.speedtest.core.base.Utils
import com.dosse.speedtest.core.base.Utils.sleep
import com.dosse.speedtest.core.config.SpeedtestConfig
import com.dosse.speedtest.core.log.Logger

abstract class PingStream(
    private val server: String,
    private val path: String,
    pings: Int,
    errorHandlingMode: String,
    connectTimeout: Int,
    soTimeout: Int,
    recvBuffer: Int,
    sendBuffer: Int,
    log: Logger?
) {
    private var remainingPings = 10
    private val connectTimeout: Int
    private val soTimeout: Int
    private val recvBuffer: Int
    private val sendBuffer: Int
    private var c: Connection? = null
    private var pinger: Pinger? = null
    private var errorHandlingMode = SpeedtestConfig.ONERROR_ATTEMPT_RESTART
    private var stopASAP = false
    private val log: Logger?
    private fun init() {
        if (stopASAP) return
        if (c != null) {
            try {
                c!!.close()
            } catch (t: Throwable) {
            }
        }
        object : Thread() {
            override fun run() {
                if (pinger != null) pinger!!.stopASAP()
                if (remainingPings <= 0) return
                try {
                    c = Connection(server, connectTimeout, soTimeout, recvBuffer, sendBuffer)
                    if (stopASAP) {
                        try {
                            c!!.close()
                        } catch (t: Throwable) {
                        }
                        return
                    }
                    pinger = object : Pinger(c!!, path) {
                        override fun onPong(ns: Long): Boolean {
                            val r = this@PingStream.onPong(ns)
                            return if (--remainingPings <= 0 || !r) {
                                onDone()
                                false
                            } else true
                        }

                        override fun onError(err: String?) {
                            log("A pinger died")
                            if (errorHandlingMode == SpeedtestConfig.ONERROR_FAIL) {
                                this@PingStream.onError(err)
                                return
                            }
                            if (errorHandlingMode == SpeedtestConfig.ONERROR_ATTEMPT_RESTART || errorHandlingMode == SpeedtestConfig.ONERROR_MUST_RESTART) {
                                Utils.sleep(100)
                                init()
                            }
                        }
                    }
                } catch (t: Throwable) {
                    log("A pinger failed hard")
                    try {
                        c!!.close()
                    } catch (t1: Throwable) {
                    }
                    if (errorHandlingMode == SpeedtestConfig.ONERROR_MUST_RESTART) {
                        Utils.sleep(100)
                        init()
                    } else onError(t.toString())
                }
            }
        }.start()
    }

    abstract fun onError(err: String?)
    abstract fun onPong(ns: Long): Boolean
    abstract fun onDone()
    fun stopASAP() {
        stopASAP = true
        if (pinger != null) pinger!!.stopASAP()
    }

    fun join() {
        while (pinger == null) sleep(0, 100)
        try {
            pinger!!.join()
        } catch (t: Throwable) {
        }
    }

    private fun log(s: String) {
        log?.l(s)
    }

    init {
        remainingPings = if (pings < 1) 1 else pings
        this.errorHandlingMode = errorHandlingMode
        this.connectTimeout = connectTimeout
        this.soTimeout = soTimeout
        this.recvBuffer = recvBuffer
        this.sendBuffer = sendBuffer
        this.log = log
        init()
    }
}