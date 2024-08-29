package Server

import core.QSslConfig
import core.RouteData
import io.undertow.Handlers.resource
import io.undertow.Undertow
import io.undertow.UndertowOptions.ENABLE_HTTP2
import io.undertow.server.HttpServerExchange
import io.undertow.server.RoutingHandler
import io.undertow.server.handlers.PathHandler
import io.undertow.server.handlers.form.FormDataParser
import io.undertow.server.handlers.resource.PathResourceManager
import io.undertow.server.handlers.resource.ResourceHandler
import io.undertow.util.HttpString
import io.undertow.util.SameThreadExecutor
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.aquiles.core.HttpHandler
import org.aquiles.core.HttpRequest
import org.aquiles.core.HttpResponse
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Paths
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
//keytool -genkeypair -alias undertow -keyalg RSA -keystore keystore.jks -storepass your_keystore_password -keypass your_key_password



class HttpServer(
    val port: Int,
    private val host: String = "0.0.0.0",
    private val routes: MutableList<RouteData>,
    private var resourceHandler: PathHandler?,
    val routingHandler: RoutingHandler,
    // Add optional parameters for keystore configuration
    private  val sslConfig : QSslConfig? = null
) {
    private lateinit var server: Undertow

    fun start() {
        // Default resource handler for other content
        resourceHandler = resourceHandler?.apply {
            addPrefixPath("/", ResourceHandler(
                PathResourceManager(Paths.get("./src/main/resources"), 100)
            ).setDirectoryListingEnabled(false))
        } ?: PathHandler().apply {
            addPrefixPath("/", ResourceHandler(
                PathResourceManager(Paths.get("./src/main/resources"), 100)
            ).setDirectoryListingEnabled(false))
        }

        val builder = Undertow.builder()
            .setServerOption(ENABLE_HTTP2, false)
            .setWorkerThreads(32 * Runtime.getRuntime().availableProcessors())
            .setHandler(resourceHandler)


        if (sslConfig != null) {
            val sslContext = createSSLContext(sslConfig.keyStorePath!!, sslConfig.keyStorePassword!!, sslConfig.keyPassword!!)

            // Check if SSLContext creation was successful
            if (sslContext != null) {
                builder.addHttpsListener(port, host, sslContext, routingHandler)
                println("Server started on https://$host:$port")
            } else {
                println("Failed to create SSLContext. Starting HTTP server instead.")
                builder.addHttpListener(port, host, routingHandler)
                println("Server started on http://$host:$port")
            }
        } else {
            builder.addHttpListener(port, host, routingHandler)
            println("Server started on http://$host:$port")
        }

        server = builder.build()
        server.start()
    }

    fun port() = port
    fun host() = host

    fun stop() = server.stop()



    private fun createSSLContext(
        keyStorePath: String,
        keyStorePassword: String,
        keyPassword: String,
        keyStoreType: String = "JKS" // Default to JKS, but allow customization
    ): SSLContext? {
        try {
            val keyStore = KeyStore.getInstance(keyStoreType)

            var keyStoreStream: InputStream? = javaClass.getResourceAsStream(keyStorePath)
            if (keyStoreStream == null) {
                keyStoreStream = FileInputStream(keyStorePath)
            }

            keyStore.load(keyStoreStream, keyStorePassword.toCharArray())

            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keyManagerFactory.init(keyStore, keyPassword.toCharArray())

            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(keyStore)

            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(keyManagerFactory.keyManagers, trustManagerFactory.trustManagers, SecureRandom())

            return sslContext
        } catch (e: Exception) {
            println("Error creating SSLContext: ${e.message}")
            return null
        }
    }}

