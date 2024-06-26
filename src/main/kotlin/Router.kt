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

class Router(vararg list: Route, private var globalMiddleware: MutableSet<HttpMiddleware> = mutableSetOf()) {

    private lateinit var server : Http4kServer
    private var app: RoutingHttpHandler



    constructor(list: List<Route>) : this(*list.toTypedArray())

    init {
        app = routes(list.asList().map {
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
     * Applies global middleware to a handler
     */
    private fun applyGlobalMiddleware(handler: HttpHandler): HttpHandler {
        if (globalMiddleware.isEmpty()) return handler

        return globalMiddleware.fold(handler) { h, middleware ->
            middleware.then(handler)
        }
    }

    /**
     * Adds a handler for a given method, path, and scope
     */
    private fun addHandler(handler: HttpHandler, method: Method, path: String, scope: RoutingScope) {

        val prefixedPath = "${formatRoutePrefix(scope.prefix)}$path"
        var h = applyScopeMiddleware(scope.scopeMiddleware(), handler)
        h = applyMiddleware(h, prefixedPath, scope.middleware())
        h = applyGlobalMiddleware(h)
        app = routes(app, prefixedPath bind method to h)
    }

    /**
     * Checks if a given type is a List of UploadFile
     */
    private fun isListUploadFile(type: KType): Boolean {
        if (type.classifier != List::class) {
            return false
        }

        val argumentType = type.arguments.firstOrNull()?.type
        return argumentType != null && argumentType.classifier == UploadFile::class
    }

    /**
     * Processes parameters from a request and maps them to function parameters
     */
    private fun processParams(parameters: List<KParameter>, req: Request, multipartFields: Array<String> = arrayOf(), multipartFiles: Array<String> = arrayOf()): MutableMap<KParameter, Any> {
        val map = mutableMapOf<KParameter, Any>()

        parameters.forEach { param ->
            param.name?.let { name ->
                val res = req.query(name) ?: req.path(name)
                if (res != null) {
                    castBasedOnType(res, param.type)?.let {
                        map[param] = it
                    }
                } else if (req.header("Content-Type") == "application/json") {




                    val gson = Gson()
                    val mapType = object : TypeToken<Map<String, Any>>() {}.type
                    val ma = gson.fromJson<Map<String, Any>>(req.bodyString(), mapType)
                    fromJson(ma ,param.type)?.let {
                        map[param] = it
                    }
  /*                  val className =  "org.aquiles.User"//param.type.classifier.toString() // Assuming you get this from somewhere

                        val userClass = Class.forName(className).kotlin


                        val constructor = userClass.primaryConstructor!!
                        val arguments = constructor.parameters.associateWith {
                            ma[it.name!!]?.let {

                                it1 ->
                                castBasedOnType(it1,it.type)
                            } // Match parameter names to JSON keys
                        }
                    val user = constructor.callBy(arguments)


                       // println(user)

                        //gson.fromJson(req.bodyString(), param.type::class.java)
*/

                } else{

                    val files = handleMultipartForm(req, files = multipartFiles, fields = multipartFields)
                    when {
                        isListUploadFile(param.type) -> map[param] = files
                        param.type.classifier == UploadFile::class && files.isNotEmpty() -> map[param] = files[0]
                    }
                }
            }
        }

        return map
    }






    @OptIn(ExperimentalStdlibApi::class)
    private fun fromJson(jsonMap: Map<String, Any>, kType: KType): Any? {
        try {
            val gson = Gson()
            val json = gson.toJson(jsonMap) // Convert map back to JSON string
            return gson.fromJson(json, kType.javaType)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    /**
     * Adds a scope to the router with optional global middleware
     */
    fun addScope(scope: RoutingScope, globalMiddleware: MutableSet<HttpMiddleware> = mutableSetOf()) {
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
    fun staticFiles(path  : String,directory : String){
        val staticFileHandler = static(
            resourceLoader = ResourceLoader.Directory(directory), // Assuming "static" folder in project root
        )
        app = routes(app, path bind Method.GET to staticFileHandler)
    }



    /**
     * Handles multipart form data from a request
     */
    private fun handleMultipartForm(request: Request, fields: Array<String>, files: Array<String>): List<UploadFile> {
        val multipartForm = MultipartFormBody.from(request)
        val list = mutableListOf<UploadFile>()

        files.forEach { field ->
            multipartForm.file(field)?.let { fileInput ->
                val up = UploadFile(fileInput.filename, fileInput.contentType, fileInput.content)
                list.add(up)
            }
        }

        return list
    }

    /**
     * Starts the HTTP server on the given port
     */
    fun start(port: Int) {
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
