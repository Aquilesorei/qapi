package org.aquiles



import io.ktor.http.*
import io.undertow.util.BadRequestException


import org.http4k.server.asServer

import org.http4k.core.*
import org.http4k.routing.*
import org.http4k.server.ServerConfig
//kotlin reflect
import kotlin.reflect.*
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.findAnnotation

import kotlinx.coroutines.*
import org.aquiles.core.HttpResponse
import org.aquiles.core.HttpStatus
import org.aquiles.core.jsonResponse
import org.aquiles.serialization.handleResponse
import org.http4k.contract.ContractRoute
import org.http4k.contract.ui.redocLite
import org.http4k.contract.ui.swaggerUiLite
import org.http4k.server.Http4kServer

class Router() {

    private lateinit var server : Http4kServer
    private var app: RoutingHttpHandler
    private var globalMiddleware: MutableSet<HttpMiddleware> = mutableSetOf()
     val employementContracts : MutableList<ContractRoute> = emptyList<ContractRoute>().toMutableList()

    private val coroutine = CoroutineScope(Dispatchers.IO + SupervisorJob()) // Use Dispatchers.IO for IO-bound tasks
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
     fun addMiddleware(middleware  : HttpMiddleware): Router {
         globalMiddleware.add(middleware);
        return this
     }


    /**
     * Adds a handler for a given method, path, and scope
     */
    private fun addHandler(function: KCallable<*>,method: Method, path: String, scope: RoutingScope, prefix : String?,multipartFields: Array<String> = arrayOf(), multipartFiles: Array<String> = arrayOf()) {


        val handler: HttpHandler = createFunctionHandler(scope,function,multipartFiles=multipartFiles, multipartFields = multipartFields)
        val prefixedPath = "${formatRoutePrefix(prefix)}$path"

        var h = applyScopeMiddleware(scope.scopeMiddleware(), handler)
        h = applyMiddleware(h, prefixedPath, scope.middleware())

        val contractRoute = createContractHandler(
            summary = convertToSentenceCase(function.name),
            path  = prefixedPath,
            handler = handler,
            method = method
        )
        employementContracts.add(contractRoute)
    }







    /**
     * Adds a scope to the router with optional global middleware
     */
    fun addScope(scope: RoutingScope, globalMiddleware: MutableSet<HttpMiddleware> = mutableSetOf(), prefix : String? = null): Router {
        this.globalMiddleware.addAll(globalMiddleware)


       val  job = coroutine.launch {val kClass = scope::class
            for (function in kClass.members) {
                val endpointAnnotation = function.findAnnotation<EndPoint>()
                if (endpointAnnotation != null) {

                    addHandler(function, endpointAnnotation.method, endpointAnnotation.path, scope = scope,prefix=prefix)
                } else {
                    when {
                        function.findAnnotation<Get>() != null -> {
                            val anon = function.findAnnotation<Get>()!!
                            val path = anon.path
                            //println("Found GET endpoint: $path on ${function.name}")

                            addHandler(function, Method.GET, anon.path, scope = scope,prefix=prefix)
                        }
                        function.findAnnotation<Post>() != null -> {
                            val anon = function.findAnnotation<Post>()!!
                            val path = anon.path
                            val multipartFiles = anon.multipartFiles
                            val multipartFields = anon.multipartFields
                            //  println("Found POST endpoint: $path on ${function.name}")
                            addHandler(function, Method.POST, path, scope = scope,prefix=prefix,multipartFields=multipartFields,multipartFiles=multipartFiles)
                        }
                        function.findAnnotation<Put>() != null -> {
                            val path = function.findAnnotation<Put>()!!.path
                            // println("Found PUT endpoint: $path on ${function.name}")
                            addHandler(function, Method.PUT, path, scope = scope,prefix=prefix)
                        }
                        function.findAnnotation<Delete>() != null -> {
                            val path = function.findAnnotation<Delete>()!!.path
                            // println("Found DELETE endpoint: $path on ${function.name}")

                            addHandler(function, Method.DELETE, path, scope = scope,prefix=prefix)

                        }
                        function.findAnnotation<Patch>() != null -> {
                            val path = function.findAnnotation<Patch>()!!.path
                            //println("Found PATCH endpoint: $path on ${function.name}")
                            addHandler(function, Method.PATCH, path, scope = scope,prefix=prefix)
                        }
                        function.findAnnotation<Head>() != null -> {
                            val path = function.findAnnotation<Head>()!!.path
                            //println("Found HEAD endpoint: $path on ${function.name}")

                            addHandler(function, Method.HEAD, path, scope = scope,prefix=prefix)
                        }
                        function.findAnnotation<Options>() != null -> {
                            val path = function.findAnnotation<Options>()!!.path
                            //println("Found OPTIONS endpoint: $path on ${function.name}")

                            addHandler(function, Method.OPTIONS, path, scope = scope,prefix=prefix)
                        }
                        function.findAnnotation<Trace>() != null -> {
                            val path = function.findAnnotation<Trace>()!!.path
                            //println("Found TRACE endpoint: $path on ${function.name}")

                            addHandler(function, Method.TRACE, path, scope = scope,prefix=prefix)
                        }
                        function.findAnnotation<Purge>() != null -> {
                            val path = function.findAnnotation<Purge>()!!.path
                            // println("Found PURGE endpoint: $path on ${function.name}")

                            addHandler(function, Method.PURGE, path, scope = scope,prefix=prefix)
                        }
                    }
                }
            }
        }

        runBlocking {
            job.join();
        }

        return this;
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


    private fun createFunctionHandler(
        scope: RoutingScope,
        function: KCallable<*>,
        multipartFields: Array<String> = arrayOf(),
        multipartFiles: Array<String> = arrayOf(),
    ): HttpHandler {
        return { req: Request ->
            val responseCompletable = CompletableDeferred<Response>()

            coroutine.launch(Dispatchers.IO) {
                try {
                    val params = processParams(function.parameters, req, multipartFields, multipartFiles)
                    params[function.instanceParameter!!] = scope
                    val res = if (function.isSuspend) function.callSuspendBy(params) else function.callBy(params)
                    responseCompletable.complete(handleResponse(res))
                } catch (e: BadRequestException) {
                    e.printStackTrace()
                    responseCompletable.complete(Response(Status.BAD_REQUEST).body(e.message ?: "Bad Request"))
                } catch (e: Exception) {
                    e.printStackTrace()
                    responseCompletable.complete(Response(Status.INTERNAL_SERVER_ERROR).body("Internal server error"))
                }
            }

            runBlocking {
                responseCompletable.await()
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
    fun start( config: ServerConfig) {

          for(f in  globalMiddleware){
              app = app.withFilter(f);
          }

        app =  routes(
            app,
            // bind the API and OpenApi description off of root
            createAll(employementContracts),

            // bind Swagger UI to the root path
            swaggerUiLite {
                url = "/openapi.json"
                pageTitle = "QuickAPI - Swagger UI"
                persistAuthorization = true
            },

            // Bind Redoc to another path
            "/redoc" bind redocLite {
                url = "/openapi.json"
                pageTitle = "QuickAPI - Redoc"
                options["disable-search"] = "true"
            }
        )
        server = app.asServer(config)
        server.start()
        println("Server started at http://localhost:${server.port()}/")
        println("Press Enter to stop the server.")
        readlnOrNull()

        // Stop the server
        stop()
    }

    /**
     * Stops the HTTP server
     */
    fun stop() {
        server.stop()
        coroutine.cancel()
    }

    fun graceFullShutDown(){
        server.stop()
    }
}
