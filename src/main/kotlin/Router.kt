package org.aquiles



import Server.HttpServer
import core.RouteData
import io.undertow.Handlers.resource
import io.undertow.server.handlers.resource.PathResourceManager
import io.undertow.server.handlers.resource.ResourceHandler
import io.undertow.util.BadRequestException


import org.http4k.server.asServer

import org.http4k.routing.*
import org.http4k.server.ServerConfig
//kotlin reflect
import kotlin.reflect.*
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.findAnnotation

import kotlinx.coroutines.*
import org.aquiles.core.*
import org.aquiles.serialization.handleResponse
import org.http4k.contract.ContractRoute
import org.http4k.contract.ui.redocLite
import org.http4k.contract.ui.swaggerUiLite
import org.http4k.routing.RoutingHttpHandler
import org.http4k.server.Http4kServer
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Pattern

class Router() {
    val allRoutes = mutableListOf<RouteData>()
    private lateinit var server : HttpServer
    private var globalMiddleware: MutableSet<HttpMiddleware> = mutableSetOf()
     private val employementContracts : MutableList<ContractRoute> = emptyList<ContractRoute>().toMutableList()

     var  resourceHandler : ResourceHandler? = null
    private val coroutine = CoroutineScope(Dispatchers.IO + SupervisorJob()) // Use Dispatchers.IO for IO-bound tasks
    init {




        globalMiddleware.add(
            HttpMiddleware{ next ->
                 {request : HttpRequest ->
                    val response = next(request)
                   // logRequest(request,response)
                    response
                }
            }
        )
    }








    /**
     * Applies middleware to a handler based on endpoint-specific middleware
     */
    private fun applyMiddleware(handler: HttpHandler, endpoint: String, middlewareMap: Map<String, List<HttpMiddleware>>): HttpHandler {
        
        var h = handler;
        middlewareMap[endpoint]?.forEach {  middleware ->
            h = middleware.then(h)
        }
        return  handler;
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
    private fun addHandler(function: KCallable<*>,method: HttpMethod, path: String, scope: RoutingScope, prefix : String?,multipartFields: Array<String> = arrayOf(), multipartFiles: Array<String> = arrayOf()) {


        val handler: HttpHandler = createFunctionHandler(scope,function,multipartFiles=multipartFiles, multipartFields = multipartFields)
        val prefixedPath = "${formatRoutePrefix(prefix)}$path"

        var h = applyScopeMiddleware(scope.scopeMiddleware(), handler)
        h = applyMiddleware(h, prefixedPath, scope.middleware())


       val  routeData = RouteData(method.name,prefixedPath,h)
        allRoutes.add(routeData)
/*
        val contractRoute = createContractHandler(
            summary = convertToSentenceCase(function.name),
            path  = prefixedPath,
            handler = h,
            method = method,
            contentType = ContentType.TEXT_PLAIN
        )
        employementContracts.add(contractRoute)*/
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

                            addHandler(function, HttpMethod.GET, anon.path, scope = scope,prefix=prefix)
                        }
                        function.findAnnotation<Post>() != null -> {
                            val anon = function.findAnnotation<Post>()!!
                            val path = anon.path
                            val multipartFiles = anon.multipartFiles
                            val multipartFields = anon.multipartFields
                            //  println("Found POST endpoint: $path on ${function.name}")
                            addHandler(function, HttpMethod.POST, path, scope = scope,prefix=prefix,multipartFields=multipartFields,multipartFiles=multipartFiles)
                        }
                        function.findAnnotation<Put>() != null -> {
                            val path = function.findAnnotation<Put>()!!.path
                            // println("Found PUT endpoint: $path on ${function.name}")
                            addHandler(function, HttpMethod.PUT, path, scope = scope,prefix=prefix)
                        }
                        function.findAnnotation<Delete>() != null -> {
                            val path = function.findAnnotation<Delete>()!!.path
                            // println("Found DELETE endpoint: $path on ${function.name}")

                            addHandler(function, HttpMethod.DELETE, path, scope = scope,prefix=prefix)

                        }
                        function.findAnnotation<Patch>() != null -> {
                            val path = function.findAnnotation<Patch>()!!.path
                            //println("Found PATCH endpoint: $path on ${function.name}")
                            addHandler(function, HttpMethod.PATCH, path, scope = scope,prefix=prefix)
                        }
                        function.findAnnotation<Head>() != null -> {
                            val path = function.findAnnotation<Head>()!!.path
                            //println("Found HEAD endpoint: $path on ${function.name}")

                            addHandler(function, HttpMethod.HEAD, path, scope = scope,prefix=prefix)
                        }
                        function.findAnnotation<Options>() != null -> {
                            val path = function.findAnnotation<Options>()!!.path
                            //println("Found OPTIONS endpoint: $path on ${function.name}")

                            addHandler(function, HttpMethod.OPTIONS, path, scope = scope,prefix=prefix)
                        }
                        function.findAnnotation<Trace>() != null -> {
                            val path = function.findAnnotation<Trace>()!!.path
                            //println("Found TRACE endpoint: $path on ${function.name}")

                            addHandler(function, HttpMethod.TRACE, path, scope = scope,prefix=prefix)
                        }
                        function.findAnnotation<Purge>() != null -> {
                            val path = function.findAnnotation<Purge>()!!.path
                            // println("Found PURGE endpoint: $path on ${function.name}")

                            addHandler(function, HttpMethod.PURGE, path, scope = scope,prefix=prefix)
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

    fun routesToRouteDataList(routes: List<Route>): List<RouteData> {
        val routeDataList = mutableListOf<RouteData>()

        fun collectRouteData(routes: List<Route>, parentPath: String = "") {
            for (route in routes) {
                val fullPath = parentPath + route.path
                routeDataList.add(RouteData(route.method.name, fullPath, route.handler))
                collectRouteData(route.childRoutes, fullPath) // Recursively collect child route data
            }
        }

        collectRouteData(routes)
        return routeDataList
    }


    fun withRoutes( vararg routes: Route, prefix : String? = null): Router  =  withRoutes(routes.asList(),prefix)


    fun withRoutes( routes: List<Route>, prefix : String? = null): Router {



        var res = routesToRouteDataList(routes)


        prefix?.let { pre ->
            val formatedPrefix = formatRoutePrefix(prefix)
          res= res.map {rd ->
               rd.setPath("${formatedPrefix}${rd.path}")
              rd
           }

        }

        allRoutes.addAll(res)

        return this;
    }


    private fun createFunctionHandler(
        scope: RoutingScope,
        function: KCallable<*>,
        multipartFields: Array<String> = arrayOf(),
        multipartFiles: Array<String> = arrayOf(),
    ): HttpHandler {
        return { req: HttpRequest ->
            val responseCompletable = CompletableDeferred<HttpResponse>()

            coroutine.launch(Dispatchers.IO) {
                try {
                    val params = processParams(function.parameters, req, multipartFields, multipartFiles)
                    params[function.instanceParameter!!] = scope
                    val res = if (function.isSuspend) function.callSuspendBy(params) else function.callBy(params)
                    responseCompletable.complete(handleResponse(res))
                } catch (e: BadRequestException) {
                    e.printStackTrace()
                    responseCompletable.complete(HttpResponse(HttpStatus.BAD_REQUEST,"Bad Request"))
                } catch (e: Exception) {
                    e.printStackTrace()
                    responseCompletable.complete(HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR,"Internal server error"))
                }
            }

            runBlocking {
                responseCompletable.await()
            }
        }
    }


    /**
     * serve all file for the directory [directory]
     * path is the http path
     */
    fun staticFiles(path  : String,directory : String): Router {


        val  directoryPath = Paths.get(directory)
        if (Files.exists(directoryPath) && Files.isDirectory(directoryPath)) {
            resourceHandler =  resource(PathResourceManager(directoryPath, 100)).setDirectoryListingEnabled(true)

        } else {
         throw  FileNotFoundException("Directory does not exist")
        }



        return this;
    }





    /**
     * Starts the HTTP server on the given port
     */
    fun start(port : Int) {

          for(md in  globalMiddleware){

              for (rt in allRoutes){


                  rt.apply {
                      handler = md.then(handler)
                  }

              }
          }

        HttpServer(4000, routes = allRoutes, resourceHandler = resourceHandler).start()
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
