package Server

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
import java.io.IOException
import java.nio.file.Paths
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
//keytool -genkeypair -alias undertow -keyalg RSA -keystore keystore.jks -storepass your_keystore_password -keypass your_key_password

class HttpServer(
    val port: Int,
    private val host: String ="0.0.0.0",
    private val routes: MutableList<RouteData>,
    private var resourceHandler: PathHandler?, val  routingHandler: RoutingHandler,

) {
    private lateinit var server: Undertow





    fun start() {

        val sslContext = createSSLContext()
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



        //val rootHandler = CoroutinesHandlerAdapter(routes);
        server = Undertow.builder()
            .addHttpListener(port, host,routingHandler)
            .setServerOption(ENABLE_HTTP2, false)
            .setWorkerThreads(32 * Runtime.getRuntime().availableProcessors())
            .setHandler(resourceHandler)
            .build()
        server.start()
        println("Server started on $host:$port")
    }
    //.setHandler(resource( PathResourceManager(Paths.get(System.getProperty("user.home")), 100))




    fun port() = port
    fun  host() = host

    fun stop() = server.stop()

    private fun createSSLContext(): SSLContext {
        val keyStorePath = "path/to/keystore.jks"
        val keyStorePassword = "your_keystore_password".toCharArray()
        val keyPassword = "your_key_password".toCharArray()

        val keyStore = KeyStore.getInstance("JKS")
        keyStore.load(javaClass.getResourceAsStream(keyStorePath), keyStorePassword)

        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        keyManagerFactory.init(keyStore, keyPassword)

        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(keyManagerFactory.keyManagers, trustManagerFactory.trustManagers, SecureRandom())
        return sslContext
    }
}






