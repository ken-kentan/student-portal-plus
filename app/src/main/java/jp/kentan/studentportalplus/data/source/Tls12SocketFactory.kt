package jp.kentan.studentportalplus.data.source

import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * Enables TLS v1.2 when creating SSLSockets.
 * <p/>
 * For some reason, android supports TLS v1.2 from API 16, but enables it by
 * default only from API 20.
 * @link https://developer.android.com/reference/javax/net/ssl/SSLSocket.html
 * @see SSLSocketFactory
 */
class Tls12SocketFactory(private val delegate: SSLSocketFactory) : SSLSocketFactory() {

    private companion object {
        val TLS_V12_ONLY = arrayOf("TLSv1.2")
    }

    override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites

    override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites

    @Throws(IOException::class)
    override fun createSocket(s: Socket?, host: String?, port: Int, isAutoClose: Boolean): Socket =
        delegate.createSocket(s, host, port, isAutoClose).enableTls12()

    @Throws(IOException::class)
    override fun createSocket(host: String?, port: Int): Socket =
        delegate.createSocket(host, port).enableTls12()

    @Throws(IOException::class)
    override fun createSocket(
        host: String?,
        port: Int,
        localHost: InetAddress?,
        localPort: Int
    ): Socket =
        delegate.createSocket(host, port, localHost, localPort).enableTls12()

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress?, port: Int): Socket =
        delegate.createSocket(host, port).enableTls12()

    @Throws(IOException::class)
    override fun createSocket(
        address: InetAddress?,
        port: Int,
        localAddress: InetAddress?,
        localPort: Int
    ): Socket =
        delegate.createSocket(address, port, localAddress, localPort).enableTls12()

    private fun Socket.enableTls12(): Socket {
        if (this is SSLSocket) {
            enabledProtocols =
                TLS_V12_ONLY
        }
        return this
    }
}
