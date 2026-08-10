import core.lib.ping.PingStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertTrue

class PingHangRegressionTest {

    /** A ping stream whose connection fails hard must not leave join() spinning. */
    @Test
    fun joinReturnsWhenTheStreamFailedToConnect() {
        val err = CountDownLatch(1)
        val stream = object : PingStream("http://127.0.0.1:1", "/empty", -1, "attempt-restart", 200, 200, -1, -1, null) {
            override fun onError(e: String?) { err.countDown() }
            override fun onPong(ns: Long) = true
            override fun onDone() {}
        }
        assertTrue(err.await(20, TimeUnit.SECONDS), "onError never fired")

        val joined = CountDownLatch(1)
        Thread { stream.join(); joined.countDown() }.start()
        assertTrue(joined.await(15, TimeUnit.SECONDS), "join() is still spinning after the stream died")
    }
}
