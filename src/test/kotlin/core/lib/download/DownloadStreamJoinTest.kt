package core.lib.download

import core.lib.config.SpeedtestConfig
import java.net.ServerSocket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DownloadStreamJoinTest {

    /** When the stream never got a connection (refused here), a stopped stream's
     * join() must return instead of waiting forever for a downloader to appear. */
    @Test
    fun joinReturnsWhenStoppedWithoutADownloader() {
        val closedPort = ServerSocket(0).use { it.localPort }

        val failed = CountDownLatch(1)
        val stream = object : DownloadStream(
            "http://127.0.0.1:$closedPort",
            "/garbage.php",
            1,
            SpeedtestConfig.ONERROR_ATTEMPT_RESTART,
            1_000,
            1_000,
            -1,
            -1,
            null,
        ) {
            override fun onError(err: String?) {
                failed.countDown()
            }
        }

        assertTrue(failed.await(10, TimeUnit.SECONDS), "connect against a closed port did not fail")
        stream.stopASAP()

        val joining = thread { stream.join() }
        joining.join(5_000)
        assertFalse(joining.isAlive, "join() still spinning after stopASAP()")
    }
}
