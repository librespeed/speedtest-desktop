package core.lib.base

import java.net.ServerSocket
import javax.net.ssl.SSLSocket
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals

class ConnectionHostnameVerificationTest {

    /** The TLS handshake is lazy, so the socket must already carry the endpoint
     * identification algorithm when the constructor returns -- without it, any
     * CA-valid certificate for any hostname is accepted. */
    @Test
    fun httpsSocketVerifiesTheHostname() {
        val server = ServerSocket(0)
        val accepting = thread {
            try {
                server.accept().close()
            } catch (_: Throwable) {
            }
        }

        try {
            val connection = Connection("https://127.0.0.1:${server.localPort}")
            val field = Connection::class.java.getDeclaredField("socket")
            field.isAccessible = true
            val socket = field.get(connection) as SSLSocket
            assertEquals("HTTPS", socket.sslParameters.endpointIdentificationAlgorithm)
            connection.close()
        } finally {
            server.close()
            accepting.join(5_000)
        }
    }
}
