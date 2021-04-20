package com.dosse.speedtest.core.download

import com.dosse.speedtest.core.base.Connection
import com.dosse.speedtest.core.base.Utils
import com.dosse.speedtest.core.base.Utils.sleep
import com.dosse.speedtest.core.config.SpeedtestConfig
import com.dosse.speedtest.core.log.Logger

abstract class DownloadStream(
    private val server: String,
    private val path: String,
    private val ckSize: Int,
    errorHandlingMode: String,
    connectTimeout: Int,
    soTimeout: Int,
    recvBuffer: Int,
    sendBuffer: Int,
    log: Logger?
) {
    private val connectTimeout: Int
    private val soTimeout: Int
    private val recvBuffer: Int
    private val sendBuffer: Int
    private var c: Connection? = null
    private var downloader: Downloader? = null
    private var errorHandlingMode = SpeedtestConfig.ONERROR_ATTEMPT_RESTART
    private var currentDownloaded: Long = 0
    private var previouslyDownloaded: Long = 0
    private var stopASAP = false
    private val log: Logger?
    private fun init() {
        if (stopASAP) return
        object : Thread() {
            override fun run() {
                if (c != null) {
                    try {
                        c!!.close()
                    } catch (t: Throwable) {
                    }
                }
                if (downloader != null) downloader!!.stopASAP()
                currentDownloaded = 0
                try {
                    c = Connection(server, connectTimeout, soTimeout, recvBuffer, sendBuffer)
                    if (stopASAP) {
                        try {
                            c!!.close()
                        } catch (t: Throwable) {
                        }
                        return
                    }
                    downloader = object : Downloader(c!!, path, ckSize) {
                        override fun onProgress(downloaded: Long) {
                            currentDownloaded = downloaded
                        }

                        override fun onError(err: String?) {
                            log("A downloader died")
                            if (errorHandlingMode == SpeedtestConfig.ONERROR_FAIL) {
                                this@DownloadStream.onError(err)
                                return
                            }
                            if (errorHandlingMode == SpeedtestConfig.ONERROR_ATTEMPT_RESTART || errorHandlingMode == SpeedtestConfig.ONERROR_MUST_RESTART) {
                                previouslyDownloaded += currentDownloaded
                                Utils.sleep(100)
                                init()
                            }
                        }
                    }
                } catch (t: Throwable) {
                    log("A downloader failed hard")
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
    fun stopASAP() {
        stopASAP = true
        if (downloader != null) downloader!!.stopASAP()
    }

    val totalDownloaded: Long
        get() = previouslyDownloaded + currentDownloaded

    fun resetDownloadCounter() {
        previouslyDownloaded = 0
        currentDownloaded = 0
        if (downloader != null) downloader!!.resetDownloadCounter()
    }

    fun join() {
        while (downloader == null) sleep(0, 100)
        try {
            downloader!!.join()
        } catch (t: Throwable) {
        }
    }

    private fun log(s: String) {
        log?.l(s)
    }

    init {
        this.errorHandlingMode = errorHandlingMode
        this.connectTimeout = connectTimeout
        this.soTimeout = soTimeout
        this.recvBuffer = recvBuffer
        this.sendBuffer = sendBuffer
        this.log = log
        init()
    }
}