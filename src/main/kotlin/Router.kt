package org.aquiles



//kotlin reflect

import Server.HttpServer
import com.google.gson.Gson
import core.CorsConfig
import core.QSslConfig
import core.RouteData
import io.undertow.Handlers
import io.undertow.Handlers.routing
import io.undertow.server.HttpServerExchange
import io.undertow.server.RoutingHandler
import io.undertow.server.handlers.PathHandler
import io.undertow.server.handlers.resource.PathResourceManager
import io.undertow.server.handlers.resource.ResourceHandler
import io.undertow.util.*
import kotlinx.coroutines.*
import openapi.*
import org.aquiles.core.*
import org.aquiles.serialization.handleResponse
import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.KCallable
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.instanceParameter


class Router {
    private val allRoutes = mutableListOf<RouteData>()
    private lateinit var server : HttpServer
    private var globalMiddleware: MutableSet<HttpMiddleware> = mutableSetOf()
    private val routingHandler: RoutingHandler = Handlers.routing()
     private var  resourceHandler : PathHandler? = null
    private val coroutine = CoroutineScope(Dispatchers.IO + SupervisorJob()) // Use Dispatchers.IO for IO-bound tasks

    private var corsConfig: CorsConfig = CorsConfig()

    // ... other methods ...

    fun withCors(config: CorsConfig): Router {
        this.corsConfig = config
        return this
    }
    init {




        globalMiddleware.add(
            HttpMiddleware{ next ->
                 {request : HttpRequest ->
                    val response = next(request)
                    logRequest(request,response)
                    response
                }
            }
        )
    }
    var specJson  : String? = null;





    private fun generateOpenAPISpec(port: Int): OpenAPISpec {
        val info = Info(
            version = "1.0.0",
            title = "QAPI",

        )

        return OpenAPIGenerator.generateOpenAPISpec(
            info,
            host = "localhost:${port}",
            schemes = listOf("https"),
            servers = listOf(
            OpenApiServer(
                url = "http://localhost:${port}",
                description = ""
            )
        )
        )
    }

    private fun printOpenAPISpec(port: Int) {
        val spec = generateOpenAPISpec(port)

        specJson = Gson().toJson(spec);

      serveDocs();

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



        val  routeData= RouteData(method.name,prefixedPath,h)

       OpenAPIGenerator.addPathItem(routeData,function,method)



        allRoutes.add(routeData)
/*
        val contractRoute = createContractHandler(
            summary = convertToSentenceCase(function.name),
            path  = prefixedPath,
            handler = h,
            method = method,
            contentType = ContentType.TEXT_PLAIN
        )
        employmentContracts.add(contractRoute)*/
    }







    /**
     * Adds a scope to the router with optional global middleware
     */
    fun addScope(scope: RoutingScope, globalMiddleware: MutableSet<HttpMiddleware> = mutableSetOf(), prefix : String? = null): Router {
        this.globalMiddleware.addAll(globalMiddleware)


       val  job = coroutine.launch {
           val kClass = scope::class
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

/*    fun routesToRouteDataList(routes: List<Route>): List<RouteData> {
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
    }*/


    fun withRoutes( vararg routes: Route, prefix : String? = null): Router  =  withRoutes(routes.asList(),prefix)


    fun withRoutes(routes: List<Route>, prefix: String? = null): Router {
        val formattedPrefix = prefix?.let { formatRoutePrefix(it) } ?: "" // Calculate prefix only once

        allRoutes.addAll(
            routes.flatMap { it.toHandler() }
                .map { it.copy(path = formattedPrefix + it.path) } // Combine flatMap & map
        )

        return this
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
                    // Check if the function has no parameters
                    if (function.parameters.isEmpty()) {
                        // Call function without any parameters
                        val res = if (function.isSuspend) function.callSuspend() else function.call()
                        responseCompletable.complete(handleResponse(res))
                    } else {
                        val params = processParams(function.parameters, req, multipartFields, multipartFiles)

                    /*    // Handle the case where the only parameter is of type HttpRequest
                        if (function.parameters.size == 1 && function.parameters.first().type.classifier == HttpRequest::class) {
                            params[function.parameters.first()] = req // Map HttpRequest to the function's parameter
                        }
*/
                        // Handle the case where there are instance parameters (e.g., bound to `scope`)
                        if (function.instanceParameter != null) {
                            params[function.instanceParameter!!] = scope
                        }

                        // Call the function with the mapped parameters
                        val res = if (function.isSuspend) function.callSuspendBy(params) else function.callBy(params)
                        responseCompletable.complete(handleResponse(res))
                    }
                } catch (e: BadRequestException) {
                    e.printStackTrace()
                    responseCompletable.complete(HttpResponse(HttpStatus.BAD_REQUEST, "Bad Request"))
                } catch (e: Exception) {
                    e.printStackTrace()
                    responseCompletable.complete(HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"))
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
        fun getFileExtension(path: Path): String {
            val fileName = path.fileName.toString()
            val dotIndex = fileName.lastIndexOf('.')
            return if (dotIndex != -1 && dotIndex < fileName.length - 1) {
                fileName.substring(dotIndex + 1)
            } else {
                ""
            }
        }

        fun serveFile(exchange: HttpServerExchange, filePath: Path) {
            val mimeMappings = MimeMappings.DEFAULT
            val fileExtension = getFileExtension(filePath)
            val mimeType = mimeMappings.getMimeType(fileExtension) ?: "application/octet-stream"

            // Set the content type
            exchange.responseHeaders.put(Headers.CONTENT_TYPE, mimeType)

            // Set up chunked transfer encoding
            exchange.responseHeaders.put(Headers.TRANSFER_ENCODING, "chunked")

            val bufferSize = 8192  // Buffer size for chunked transfer
            val buffer = ByteBuffer.allocate(bufferSize)

            // Use FileInputStream to read the file
            FileInputStream(filePath.toFile()).use { inputStream ->
                val channel = Channels.newChannel(inputStream)
                val sinkChannel = exchange.responseChannel

                // Read file in chunks and write to the response
                while (channel.read(buffer) != -1) {
                    buffer.flip()
                    while (buffer.hasRemaining()) {
                        sinkChannel.write(buffer)
                    }
                    buffer.clear()
                }
            }

            // End the exchange
            exchange.endExchange()
        }



        val  directoryPath = Paths.get(directory)



        if (Files.exists(directoryPath) && Files.isDirectory(directoryPath)) {


            routingHandler.add(
                HttpString("GET"),
                "/$path/{fileName}"
            ) { exchange ->


                exchange.dispatch(SameThreadExecutor.INSTANCE, Runnable {
                    coroutine.launch {
                        val fileName = exchange.queryParameters["fileName"]?.firstOrNull()
                        if (fileName != null) {
                            val filePath = directoryPath.resolve(fileName)
                            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                                serveFile(exchange, filePath)
                            } else {
                                exchange.setStatusCode(404).endExchange()
                            }
                        } else {
                            exchange.setStatusCode(400).endExchange()
                        }
                    }
                })

            }
        } else {
         throw  FileNotFoundException("Directory does not exist")
        }



        return this;
    }





    /**
     * Starts the HTTP server on the given port
     */
    fun start(port : Int,sslConfig : QSslConfig? =null) {

        for(md in  globalMiddleware){

              for (rt in allRoutes){


                  rt.apply {
                      handler = md.then(handler)
                  }

              }
          }

        allRoutes.forEach { routeData ->
                routingHandler.add(
                    HttpString(routeData.method),
                    routeData.path
                ) { exchange ->


                    exchange.dispatch(SameThreadExecutor.INSTANCE, Runnable {
                        coroutine.launch {
                            sendHttpResponse(exchange,routeData.handler(HttpRequest.from(exchange)) )
                        }
                    })

                }
        }

        printOpenAPISpec(port)
       server = HttpServer(port, routes = allRoutes, resourceHandler = resourceHandler, routingHandler = routingHandler, sslConfig = sslConfig, corsConfig = corsConfig)
        server.start()
        println("Server started at http${if (sslConfig != null ) "s" else ""}://localhost:${server.port()}/")
        println("Press Enter to stop the server.")
        readlnOrNull()

        // Stop the server
        stop()
    }






    private fun serveDocs(){


        fun serve(path: String,exchange: HttpServerExchange,contentType : String){

            exchange.dispatch(SameThreadExecutor.INSTANCE, Runnable {
                coroutine.launch {
                    //val filePath = Paths.get(path)

                    exchange.startBlocking()

                    try {


                        exchange.responseHeaders.put(Headers.CONTENT_TYPE, contentType)

                        val classLoader = this::class.java.classLoader // Or use another appropriate class loader

                        var fileInputStream: InputStream? = classLoader.getResourceAsStream(path)
                        if (fileInputStream == null) {
                            fileInputStream = FileInputStream(path)
                        }

                        fileInputStream.use { inputStream ->
                            inputStream.transferTo(exchange.outputStream)
                        }
                    } catch (e: IOException) {
                        exchange.statusCode = 500
                        e.printStackTrace()  // Log the error for debugging purposes
                    } finally {
                        if (!exchange.isComplete) {
                            exchange.endExchange()
                        }
                    }
                }

            })

        }

        fun serveText(content: String, exchange: HttpServerExchange, contentType: String) {
            exchange.dispatch(SameThreadExecutor.INSTANCE, Runnable {
                coroutine.launch {
                    exchange.startBlocking()

                    try {
                        exchange.responseHeaders.put(Headers.CONTENT_TYPE, contentType)
                        exchange.responseSender.send(content) // Send the text content
                    } catch (e: IOException) {
                        exchange.statusCode = 500
                        e.printStackTrace()
                    } finally {
                        if (!exchange.isComplete) {
                            exchange.endExchange()
                        }
                    }
                }
            })
        }

        routingHandler.add(HttpString("GET"), "/docs") { exchange ->

            serve("index.html",exchange,"text/html")
        }
        routingHandler.add(HttpString("GET"), "/openapi.json") { exchange ->
            serveText(specJson!!,exchange,"application/json")
        }
    }
    private fun sendHttpResponse(exchange: HttpServerExchange, response: HttpResponse) {
        // Set CORS headers early


        exchange.statusCode = response.statusCode.code

        response.headers.forEach { (name, value) ->
            exchange.responseHeaders.put(HttpString(name), value.toString())
        }


            val inputStream = response.body.stream
            val sinkChannel = exchange.responseChannel
            val buffer = ByteBuffer.allocate(8192)  // Buffer size

            inputStream.use {
                val channel = Channels.newChannel(inputStream)
                while (channel.read(buffer) != -1) {
                    buffer.flip()
                    while (buffer.hasRemaining()) {
                        sinkChannel.write(buffer)
                    }
                    buffer.clear()
                }
            }

            exchange.endExchange()


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
