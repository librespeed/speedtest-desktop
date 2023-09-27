package core.lib.download

import core.lib.base.Connection
import core.lib.base.Utils.url_sep

abstract class Downloader(private val c: Connection, private val path: String, ckSize: Int) :
    Thread() {
    private val ckSize: Int
    private var stopASAP = false
    private var resetASAP = false
    private var totDownloaded: Long = 0
    override fun run() {
        try {
            var s = path
            s += url_sep(s) + "ckSize=" + ckSize
            var lastProgressEvent = System.currentTimeMillis()
            val ckBytes = (ckSize * 1048576).toLong()
            val newRequestThreshold = ckBytes / 4
            var bytesLeft: Long = 0
            val `in` = c.inputStream
            val buf = ByteArray(BUFFER_SIZE)
            while (true) {
                if (stopASAP) break
                if (bytesLeft <= newRequestThreshold) {
                    c.GET(s, true)
                    bytesLeft += ckBytes
                }
                if (stopASAP) break
                val l = `in`!!.read(buf)
                if (stopASAP) break
                bytesLeft -= l.toLong()
                if (resetASAP) {
                    totDownloaded = 0
                    resetASAP = false
                }
                totDownloaded += l.toLong()
                if (System.currentTimeMillis() - lastProgressEvent > 200) {
                    lastProgressEvent = System.currentTimeMillis()
                    onProgress(totDownloaded)
                }
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

    abstract fun onProgress(downloaded: Long)
    abstract fun onError(err: String?)
    fun resetDownloadCounter() {
        resetASAP = true
    }

    val downloaded: Long
        get() = if (resetASAP) 0 else totDownloaded

    companion object {
        private const val BUFFER_SIZE = 16384
    }

    init {
        this.ckSize = if (ckSize < 1) 1 else ckSize
        start()
    }
}