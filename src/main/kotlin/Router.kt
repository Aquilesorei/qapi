package org.aquiles

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.Undertow
import org.http4k.server.asServer
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import org.aquiles.EndPoint
import org.http4k.core.Request
import org.http4k.core.Response

class Router( vararg list: Route) {



    constructor(list: List<Route>) : this(*list.toTypedArray())



    private var  app :  RoutingHttpHandler = routes(list.asList().map {
        it.toHandler() }.toList()
    )


  private fun addHandler(handler: (Request) -> Response, method: Method, path: String, ) {
      app = routes(app, path bind method to handler)
  }

    fun addAnnotatedHandler(handler: Any) {

        val kClass = handler::class
        for (function in kClass.members) {
            function.findAnnotation<EndPoint>()?.let { endpoint ->
                println("Found endpoint: ${endpoint.method} ${endpoint.path} on ${function.name}")
                val functionHandler = { req: Request ->
                    val res = function.call(handler,req)
                   res   as Response
                }
                addHandler(functionHandler,endpoint.method, endpoint.path)
            }
        }

    }


    private lateinit var server : Http4kServer;


    fun start(port: Int ) {
        server = app.asServer(Undertow(port))

        server.start()
        println("Server started on port http://localhost:$port/")
        println("Press Enter to stop the server.")
        readlnOrNull()

        // Stop the server
        stop()
    }

    fun stop() = server.stop()
}