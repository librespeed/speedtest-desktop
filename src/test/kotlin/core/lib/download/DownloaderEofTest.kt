package core.lib.download

import core.lib.base.Connection
import java.net.ServerSocket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertTrue

class DownloaderEofTest {

    /** A server that closes the keep-alive connection must fail the downloader,
     * not send it into a busy-spin with a negative byte counter. */
    @Test
    fun serverClosingTheConnectionRaisesOnError() {
        val server = ServerSocket(0)
        val serving = thread {
            try {
                server.accept().use { socket ->
                    socket.getOutputStream().apply {
                        write(ByteArray(100))
                        flush()
                    }
                }
            } catch (_: Throwable) {
            }
        }

        val failed = CountDownLatch(1)
        val connection = Connection("http://127.0.0.1:${server.localPort}")
        val downloader = object : Downloader(connection, "/garbage.php", 1) {
            override fun onProgress(downloaded: Long) {}
            override fun onError(err: String?) {
                failed.countDown()
            }
        }

        try {
            assertTrue(failed.await(10, TimeUnit.SECONDS), "EOF did not surface as an error")
            assertTrue(downloader.downloaded >= 0, "byte counter went negative: ${downloader.downloaded}")
        } finally {
            downloader.stopASAP()
            connection.close()
            server.close()
            serving.join(5_000)
        }
    }
}
