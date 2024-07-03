package Server

import core.RouteData
import io.undertow.Undertow
import io.undertow.UndertowOptions.ENABLE_HTTP2
import io.undertow.server.HttpServerExchange
import io.undertow.server.handlers.form.FormDataParser
import io.undertow.server.handlers.resource.ResourceHandler
import io.undertow.util.HttpString
import io.undertow.util.SameThreadExecutor
import jdk.internal.joptsimple.internal.Strings
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.aquiles.core.HttpHandler
import org.aquiles.core.HttpRequest
import org.aquiles.core.HttpResponse
import org.http4k.multipart.MultipartFile
import java.io.IOException


class HttpServer(val port: Int, private val host: String ="0.0.0.0", private val routes: MutableList<RouteData>,
                 private val resourceHandler: ResourceHandler?) {
    private lateinit var server: Undertow





    fun start() {
         val rootHandler = CoroutinesHandlerAdapter(routes);
        server = Undertow.builder()
            .addHttpListener(port, host,rootHandler)
            .setServerOption(ENABLE_HTTP2, false)
            .setWorkerThreads(32 * Runtime.getRuntime().availableProcessors())
            .setHandler(resourceHandler)
            .build()
        server.start()
        println("Server started on $host:$port")
    }




    fun port() = port
    fun  host() = host

    fun stop() = server.stop()






}




class CoroutinesHandlerAdapter(private val routes: MutableList<RouteData>) :  io.undertow.server.HttpHandler {

   // private val handler: CoroutinesHandler
    @OptIn(DelicateCoroutinesApi::class)
    override fun handleRequest(exchange: HttpServerExchange) {
        exchange.dispatch(SameThreadExecutor.INSTANCE, Runnable {
            GlobalScope.launch(Dispatchers.Default) {
                val request = HttpRequest.from(exchange)
                val response = handleRequest(request)
                sendHttpResponse(exchange, response)
            }
        })
    }



    private fun sendHttpResponse(exchange: HttpServerExchange, response: HttpResponse) {
        println(exchange.requestPath)
        exchange.statusCode = response.statusCode.code
        response.headers.forEach { (name, value) ->
            exchange.responseHeaders.put(HttpString(name), value.toString())
        }


        exchange.outputStream.write(response.body.stream.readAllBytes())
    }

    private fun handleRequest(request: HttpRequest): HttpResponse {
        val  handler = findHandler(request)
        val response = handler(request)
        return response
    }

    private fun findHandler(request: HttpRequest): HttpHandler {
        for (route in routes) {
            if (route.matches(request)) {
                return route.handler
            }
        }
         throw Exception("route not found")
    }


}


