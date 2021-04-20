package com.dosse.speedtest.core.upload

import com.dosse.speedtest.core.base.Connection
import java.util.*

abstract class Uploader(private val c: Connection, private val path: String, ckSize: Int) :
    Thread() {
    private var stopASAP = false
    private var resetASAP = false
    private var totUploaded: Long = 0
    private val garbage: ByteArray
    override fun run() {
        try {
            val s = path
            var lastProgressEvent = System.currentTimeMillis()
            val out = c.outputStream
            val buf = ByteArray(BUFFER_SIZE)
            while (true) {
                if (stopASAP) break
                c.POST(s, true, "application/octet-stream", garbage.size.toLong())
                var offset = 0
                while (offset < garbage.size) {
                    if (stopASAP) break
                    val l = if (offset + BUFFER_SIZE >= garbage.size) garbage.size - offset else BUFFER_SIZE
                    out!!.write(garbage, offset, l)
                    if (stopASAP) break
                    if (resetASAP) {
                        totUploaded = 0
                        resetASAP = false
                    }
                    totUploaded += l.toLong()
                    if (System.currentTimeMillis() - lastProgressEvent > 200) {
                        lastProgressEvent = System.currentTimeMillis()
                        onProgress(totUploaded)
                    }
                    offset += BUFFER_SIZE
                }
                if (stopASAP) break
                while (!c.readLineUnbuffered()!!.trim { it <= ' ' }.isEmpty());
            }
            c.close()
        } catch (t: Throwable) {
            try {
                c.close()
            } catch (t1: Throwable) {
            }
            onError(t.toString())
        }
    }

    fun stopASAP() {
        stopASAP = true
    }

    abstract fun onProgress(uploaded: Long)
    abstract fun onError(err: String?)
    fun resetUploadCounter() {
        resetASAP = true
    }

    val uploaded: Long
        get() = if (resetASAP) 0 else totUploaded

    companion object {
        private const val BUFFER_SIZE = 16384
    }

    init {
        garbage = ByteArray(ckSize * 1048576)
        val r = Random(System.nanoTime())
        r.nextBytes(garbage)
        start()
    }
}