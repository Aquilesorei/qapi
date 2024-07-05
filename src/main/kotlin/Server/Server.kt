package Server

import core.RouteData
import io.undertow.Handlers.resource
import io.undertow.Undertow
import io.undertow.UndertowOptions.ENABLE_HTTP2
import io.undertow.server.HttpServerExchange
import io.undertow.server.RoutingHandler
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
import org.http4k.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Paths


class HttpServer(val port: Int, private val host: String ="0.0.0.0", private val routes: MutableList<RouteData>,
                 private val resourceHandler: ResourceHandler?, val  routingHandler: RoutingHandler
) {
    private lateinit var server: Undertow





    fun start() {

     /*   val router = io.undertow.Handlers.routing()

        // Route with a path parameter
        router.get("/users/{userId}") { exchange ->
            val userId = exchange.pathParameters["userId"]?.firstOrNull()

            if (userId != null) {
                // Fetch user data based on userId (e.g., from a database)
                val userData = getUserData(userId)

                if (userData != null) {
                    exchange.responseHeaders.put(Headers.CONTENT_TYPE, "application/json")
                    exchange.statusCode = StatusCodes.OK
                    exchange.responseSender.send(userData.toString()) // Assuming userData can be serialized to JSON
                } else {
                    exchange.statusCode = StatusCodes.NOT_FOUND
                    exchange.responseSender.send("User not found")
                }
            } else {
                exchange.statusCode = StatusCodes.BAD_REQUEST
                exchange.responseSender.send("Invalid user ID")
            }
        }*/

        //val rootHandler = CoroutinesHandlerAdapter(routes);
        server = Undertow.builder()
            .addHttpListener(port, host,routingHandler)
            .setServerOption(ENABLE_HTTP2, false)
            .setWorkerThreads(32 * Runtime.getRuntime().availableProcessors())
            .setHandler(resourceHandler)
            .setHandler(resource( PathResourceManager(Paths.get(System.getProperty("user.home")), 100))
            .setDirectoryListingEnabled(true))
            .build()
        server.start()
        println("Server started on $host:$port")
    }




    fun port() = port
    fun  host() = host

    fun stop() = server.stop()


}






