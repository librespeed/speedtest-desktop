package util

import java.net.*

object NetUtils {

    fun parseMacAddress(mac: ByteArray): String {
        val sb = StringBuilder()
        for (i in mac.indices) {
            sb.append(String.format("%02X%s", mac[i], if ((i < mac.size - 1)) ":" else ""))
        }
        return sb.toString()
    }

    fun getDefaultNetworkInterface(): NetworkInterface? {
        val globalHost = "a.root-servers.net"
        var result: NetworkInterface? = null
        var remoteAddress: InetAddress? = null
        try {
            remoteAddress = InetAddress.getByName(globalHost)
        } catch (ignored: UnknownHostException) {
        }
        if (remoteAddress != null) {
            try {
                DatagramSocket().use { s ->
                    try {
                        s.connect(remoteAddress, 80)
                        result = NetworkInterface.getByInetAddress(s.localAddress)
                    } catch (e : Exception) {
                        return null
                    }
                }
            } catch (ignored: SocketException) {
                return null
            }
        }
        return result
    }

}