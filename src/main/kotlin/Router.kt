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
import org.http4k.routing.path
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.instanceParameter

class Router( vararg list: Route) {

    private lateinit var server : Http4kServer;


    constructor(list: List<Route>) : this(*list.toTypedArray())



    private var  app :  RoutingHttpHandler = routes(list.asList().map {
        it.toHandler() }.toList()
    )


  private fun addHandler(handler: (Request) -> Response, method: Method, path: String, ) {
      app = routes(app, path bind method to handler)
  }

    //todo use functions.callBy to pass a map of parameter value


    private fun processParams(parameters: List<KParameter>, req : Request): MutableMap<KParameter, Any> {
        val map = mutableMapOf<KParameter,Any>()
        parameters.forEach { param ->

           param.name?.let {name ->
               println(param.name)
            var res = req.query(name);
             if(res == null) {
                 res = req.path(name)
                 if(res == null){
                     // not found
                 }else{
                     map[param] = res
                 }
             }else{
                 map[param] = res

             }
           }

        }


       return map

    }
    fun addAnnotatedHandler(handler: RoutingScope) {
        val kClass = handler::class
        for (function in kClass.members) {

            val endpointAnnotation = function.findAnnotation<EndPoint>()
            if (endpointAnnotation != null) {
                println("Found endpoint: ${endpointAnnotation.method} ${endpointAnnotation.path} on ${function.name}")
                val functionHandler = { req: Request ->
                    val res = function.call(handler, req)
                    res as Response
                }
                addHandler(functionHandler, endpointAnnotation.method, endpointAnnotation.path)
            } else {
                when {
                    function.findAnnotation<Get>() != null -> {


                        function.parameters.forEach { param ->
                            println("Found GET endpoint: ${function.name} with parameter ${param.name} with type ${param.type}")
                        }
                        val path = function.findAnnotation<Get>()!!.path
                        println("Found GET endpoint: $path on ${function.name}")
                        val functionHandler = { req: Request ->
                           val  params = processParams(function.parameters,req)
                            params[function.instanceParameter!!] = handler;
                            val res = function.callBy(params)
                            res as Response
                        }
                        addHandler(functionHandler, Method.GET, path)
                    }
                    function.findAnnotation<Post>() != null -> {
                        val path = function.findAnnotation<Post>()!!.path
                        println("Found POST endpoint: $path on ${function.name}")
                        val functionHandler = { req: Request ->
                            val res = function.call(handler, req)
                            res as Response
                        }
                        addHandler(functionHandler, Method.POST, path)
                    }
                    // Add support for other HTTP methods
                    function.findAnnotation<Put>() != null -> {
                        val path = function.findAnnotation<Put>()!!.path
                        println("Found PUT endpoint: $path on ${function.name}")
                        val functionHandler = { req: Request ->
                            val res = function.call(handler, req)
                            res as Response
                        }
                        addHandler(functionHandler, Method.PUT, path)
                    }
                    function.findAnnotation<Delete>() != null -> {
                        val path = function.findAnnotation<Delete>()!!.path
                        println("Found DELETE endpoint: $path on ${function.name}")
                        val functionHandler = { req: Request ->
                            val res = function.call(handler, req)
                            res as Response
                        }
                        addHandler(functionHandler, Method.DELETE, path)
                    }
                    function.findAnnotation<Patch>() != null -> {
                        val path = function.findAnnotation<Patch>()!!.path
                        println("Found PATCH endpoint: $path on ${function.name}")
                        val functionHandler = { req: Request ->
                            val res = function.call(handler, req)
                            res as Response
                        }
                        addHandler(functionHandler, Method.PATCH, path)
                    }
                    function.findAnnotation<Head>() != null -> {
                        val path = function.findAnnotation<Head>()!!.path
                        println("Found HEAD endpoint: $path on ${function.name}")
                        val functionHandler = { req: Request ->
                            val res = function.call(handler, req)
                            res as Response
                        }
                        addHandler(functionHandler, Method.HEAD, path)
                    }
                    function.findAnnotation<Options>() != null -> {
                        val path = function.findAnnotation<Options>()!!.path
                        println("Found OPTIONS endpoint: $path on ${function.name}")
                        val functionHandler = { req: Request ->
                            val res = function.call(handler, req)
                            res as Response
                        }
                        addHandler(functionHandler, Method.OPTIONS, path)
                    }
                    function.findAnnotation<Trace>() != null -> {
                        val path = function.findAnnotation<Trace>()!!.path
                        println("Found TRACE endpoint: $path on ${function.name}")
                        val functionHandler = { req: Request ->
                            val res = function.call(handler, req)
                            res as Response
                        }
                        addHandler(functionHandler, Method.TRACE, path)
                    }
                    function.findAnnotation<Purge>() != null -> {
                        val path = function.findAnnotation<Purge>()!!.path
                        println("Found Purge endpoint: $path on ${function.name}")
                        val functionHandler = { req: Request ->
                            val res = function.call(handler, req)
                            res as Response
                        }
                        addHandler(functionHandler, Method.PURGE, path)
                    }
                }
            }
        }
    }





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