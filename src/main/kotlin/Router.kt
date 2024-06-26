package org.aquiles


import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.undertow.util.BadRequestException
import org.http4k.server.Http4kServer
import org.http4k.server.Undertow
import org.http4k.server.asServer
import kotlin.reflect.full.findAnnotation
import org.http4k.core.*
import org.http4k.routing.*
import kotlin.reflect.*
import kotlin.reflect.full.instanceParameter

class Router() {

    private lateinit var server : Http4kServer
    private var app: RoutingHttpHandler
    private var globalMiddleware: MutableSet<HttpMiddleware> = mutableSetOf()



    init {
        app = routes(emptyList<Route>().map {
            it.toHandler()
        }.toList())



        globalMiddleware.add(
            HttpMiddleware {
                    next: HttpHandler -> {
                    request: Request ->
                val response = next(request)
                logRequest(request,response)
                response
            }
            }
        )
    }

    /**
     * Returns the app as a RoutingHttpHandler
     */
    fun asHttpHandler(): RoutingHttpHandler {
        return app
    }

    /**
     * Applies middleware to a handler based on endpoint-specific middleware
     */
    private fun applyMiddleware(handler: HttpHandler, endpoint: String, middlewareMap: Map<String, List<HttpMiddleware>>): HttpHandler {
        return middlewareMap[endpoint]?.fold(handler) { h, middleware ->
            middleware.then(h)
        } ?: handler
    }

    /**
     * Applies middleware at the scope level to a handler
     */
    private fun applyScopeMiddleware(list: Array<HttpMiddleware>, handler: HttpHandler): HttpHandler {
        if (list.isEmpty()) return handler

        return list.fold(handler) { h, middleware ->
            middleware.then(h)
        }
    }


    /**
     *
     * adds a global middleware
     */
     fun global(middleware  : HttpMiddleware): Router {
         globalMiddleware.add(middleware);
        return this
     }

    /**
     * Adds a handler for a given method, path, and scope
     */
    private fun addHandler(handler: HttpHandler, method: Method, path: String, scope: RoutingScope, prefix : String? = null) {

        val prefixedPath = "${formatRoutePrefix(prefix)}$path"
        var h = applyScopeMiddleware(scope.scopeMiddleware(), handler)
        h = applyMiddleware(h, prefixedPath, scope.middleware())
        app = routes(app, prefixedPath bind method to h)
    }




    fun withRoutes( vararg routes: Route, prefix : String? = null): Router  =  withRoutes(routes.asList(),prefix)


    fun withRoutes( routes: List<Route>, prefix : String? = null): Router {


        var combinedHandler: RoutingHttpHandler = routes(routes.map {
            it.toHandler()
        }.toList())

        prefix?.let {
           combinedHandler = combinedHandler.withBasePath(it)
        }
        app = routes(app, combinedHandler)
        return this;
    }






    /**
     * Adds a scope to the router with optional global middleware
     */
    fun addScope(scope: RoutingScope, globalMiddleware: MutableSet<HttpMiddleware> = mutableSetOf(), prefix : String? = null): Router {
        this.globalMiddleware.addAll(globalMiddleware)

        val kClass = scope::class
        for (function in kClass.members) {
            val endpointAnnotation = function.findAnnotation<EndPoint>()
            if (endpointAnnotation != null) {

                val functionHandler = createFunctionHandler(scope,function)
                addHandler(functionHandler, Method.POST, endpointAnnotation.path, scope = scope)
            } else {
                when {
                    function.findAnnotation<Get>() != null -> {
                        val anon = function.findAnnotation<Get>()!!
                        val path = anon.path
                        //println("Found GET endpoint: $path on ${function.name}")
                        val functionHandler =  createFunctionHandler(scope,function)
                        addHandler(functionHandler, Method.GET, anon.path, scope = scope)
                    }
                    function.findAnnotation<Post>() != null -> {
                        val anon = function.findAnnotation<Post>()!!
                        val path = anon.path
                        val multipartFiles = anon.multipartFiles
                        val multipartFields = anon.multipartFields
                      //  println("Found POST endpoint: $path on ${function.name}")
                        val functionHandler = { req: Request ->
                            val params = processParams(function.parameters, req, multipartFields = multipartFields, multipartFiles = multipartFiles)
                            params[function.instanceParameter!!] = scope
                            try {
                                val res = function.callBy(params)
                                try {
                                    val casted =res as Response
                                    casted
                                }catch (e : ClassCastException){
                                    println("Internal server error: Please ensure proper handling and return of Response in the handler function '${function.name}' within the scope '${scope::class.simpleName}' \n")
                                    e.printStackTrace()
                                    Response(Status.INTERNAL_SERVER_ERROR).body("Internal server error")
                                }

                            }catch (e : Exception){

                                e.printStackTrace()
                                Response(Status.INTERNAL_SERVER_ERROR).body("Internal server error\n")
                            }
                        }
                        addHandler(functionHandler, Method.POST, path, scope = scope)
                    }
                    function.findAnnotation<Put>() != null -> {
                        val path = function.findAnnotation<Put>()!!.path
                       // println("Found PUT endpoint: $path on ${function.name}")
                        val functionHandler = createFunctionHandler(scope,function)
                        addHandler(functionHandler, Method.PUT, path, scope = scope)
                    }
                    function.findAnnotation<Delete>() != null -> {
                        val path = function.findAnnotation<Delete>()!!.path
                       // println("Found DELETE endpoint: $path on ${function.name}")
                        val functionHandler =  createFunctionHandler(scope,function)
                        addHandler(functionHandler, Method.DELETE, path, scope = scope)

                    }
                    function.findAnnotation<Patch>() != null -> {
                        val path = function.findAnnotation<Patch>()!!.path
                        //println("Found PATCH endpoint: $path on ${function.name}")
                        val functionHandler = createFunctionHandler(scope,function)
                        addHandler(functionHandler, Method.PATCH, path, scope = scope)
                    }
                    function.findAnnotation<Head>() != null -> {
                        val path = function.findAnnotation<Head>()!!.path
                        //println("Found HEAD endpoint: $path on ${function.name}")
                        val functionHandler =  createFunctionHandler(scope,function)
                        addHandler(functionHandler, Method.HEAD, path, scope = scope)
                    }
                    function.findAnnotation<Options>() != null -> {
                        val path = function.findAnnotation<Options>()!!.path
                        //println("Found OPTIONS endpoint: $path on ${function.name}")
                        val functionHandler =  createFunctionHandler(scope,function)
                        addHandler(functionHandler, Method.OPTIONS, path, scope = scope)
                    }
                    function.findAnnotation<Trace>() != null -> {
                        val path = function.findAnnotation<Trace>()!!.path
                        //println("Found TRACE endpoint: $path on ${function.name}")
                        val functionHandler =  createFunctionHandler(scope,function)
                        addHandler(functionHandler, Method.TRACE, path, scope = scope)
                    }
                    function.findAnnotation<Purge>() != null -> {
                        val path = function.findAnnotation<Purge>()!!.path
                       // println("Found PURGE endpoint: $path on ${function.name}")
                        val functionHandler =  createFunctionHandler(scope,function)
                        addHandler(functionHandler, Method.PURGE, path, scope = scope)
                    }
                }
            }
        }

        return this;
    }



    private fun createFunctionHandler(scope: RoutingScope, function: KCallable<*>): HttpHandler {
        return { req: Request ->
            try {
                val params = processParams(function.parameters, req)
                params[function.instanceParameter!!] = scope
                val res = function.callBy(params)
                res as Response
            } catch (e: BadRequestException) {
                e.printStackTrace();
                Response(Status.BAD_REQUEST).body(e.message ?: "Bad Request")
            } catch (e: Exception) {
                e.printStackTrace()
                Response(Status.INTERNAL_SERVER_ERROR).body("Internal server error")
            }
        }
    }
    fun staticFiles(path  : String,directory : String): Router {
        val staticFileHandler = static(
            resourceLoader = ResourceLoader.Directory(directory), // Assuming "static" folder in project root
        )
        app = routes(app, path bind Method.GET to staticFileHandler)
        return this;
    }





    /**
     * Starts the HTTP server on the given port
     */
    fun start(port: Int) {

          for(f in  globalMiddleware){
              app = app.withFilter(f);
          }
        server = app.asServer(Undertow(port))
        server.start()
        println("Server started on port http://localhost:$port/")
        println("Press Enter to stop the server.")
        readlnOrNull()

        // Stop the server
        stop()
    }

    /**
     * Stops the HTTP server
     */
    fun stop() = server.stop()
}
