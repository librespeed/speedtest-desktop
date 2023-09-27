package core.lib.upload

import core.lib.base.Connection
import core.lib.base.Utils
import core.lib.base.Utils.sleep
import core.lib.config.SpeedtestConfig
import core.lib.log.Logger

abstract class UploadStream(
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
    private var uploader: Uploader? = null
    private var errorHandlingMode = SpeedtestConfig.ONERROR_ATTEMPT_RESTART
    private var currentUploaded: Long = 0
    private var previouslyUploaded: Long = 0
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
                if (uploader != null) uploader!!.stopASAP()
                currentUploaded = 0
                try {
                    c = Connection(server, connectTimeout, soTimeout, recvBuffer, sendBuffer)
                    if (stopASAP) {
                        try {
                            c!!.close()
                        } catch (t: Throwable) {
                        }
                        return
                    }
                    uploader = object : Uploader(c!!, path, ckSize) {
                        override fun onProgress(uploaded: Long) {
                            currentUploaded = uploaded
                        }

                        override fun onError(err: String?) {
                            log("An uploader died")
                            if (errorHandlingMode == SpeedtestConfig.ONERROR_FAIL) {
                                this@UploadStream.onError(err)
                                return
                            }
                            if (errorHandlingMode == SpeedtestConfig.ONERROR_ATTEMPT_RESTART || errorHandlingMode == SpeedtestConfig.ONERROR_MUST_RESTART) {
                                previouslyUploaded += currentUploaded
                                Utils.sleep(100)
                                init()
                            }
                        }
                    }
                } catch (t: Throwable) {
                    log("An uploader failed hard")
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
        if (uploader != null) uploader!!.stopASAP()
    }

    val totalUploaded: Long
        get() = previouslyUploaded + currentUploaded

    fun resetUploadCounter() {
        previouslyUploaded = 0
        currentUploaded = 0
        if (uploader != null) uploader!!.resetUploadCounter()
    }

    fun join() {
        while (uploader == null) sleep(0, 100)
        try {
            uploader!!.join()
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