package org.aquiles

import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.Undertow
import org.http4k.server.asServer
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import org.aquiles.EndPoint
import org.http4k.core.*
import org.http4k.routing.path
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.instanceParameter

class Router( vararg list: Route) {

    private lateinit var server : Http4kServer;
    private var  app :  RoutingHttpHandler;



    constructor(list: List<Route>) : this(*list.toTypedArray())





    init{
        app  =  routes(list.asList().map {
            it.toHandler() }.toList()
        )
    }


    public fun asHttpHandler(): RoutingHttpHandler {
        return app
    }

  private fun addHandler(handler: (Request) -> Response, method: Method, path: String, prefix :String? = null) {
      fun formatRoutePrefix(prefix: String?): String {
          return prefix?.let {
              if (it.isEmpty()) {
                  "" // Handle empty prefix case explicitly
              } else {
                  if (it.startsWith("/")) it else "/$it"
              }
          } ?: "" // Return empty string for null prefix
      }


      app = routes(app, "${formatRoutePrefix(prefix)}$path" bind method to handler)
  }

    private fun isListUploadFile(type: KType): Boolean {
        // Check if the classifier of the type is List
        if (type.classifier != List::class) {
            return false
        }

        // Check if the type argument of the List is UploadFile
        val argumentType = type.arguments.firstOrNull()?.type
        return argumentType != null && argumentType.classifier == UploadFile::class
    }

    private fun processParams(parameters: List<KParameter>, req: Request, multipartFields: Array<String>  = arrayOf(), multipartFiles: Array<String> = arrayOf()): MutableMap<KParameter, Any> {
        val map = mutableMapOf<KParameter, Any>()





        parameters.forEach { param ->

            param.name?.let { name ->

                val res = req.query(name) ?: req.path(name)

                if (res != null) {

                    castBasedOnType(res, param.type)?.let {
                        map[param] = it
                    }


                } else {


                    // Handle case where parameter is not found in query or path
                    val files = handleMultipartForm(req, files = multipartFiles, fields = multipartFields)
                    if(param.type.classifier is  KClass<*>){

                        when(param.type.classifier){
                            List::class -> {

                               if(isListUploadFile(param.type)){
                                   map[param] = files
                               }



                            }
                            UploadFile::class -> {

                                if(files.isNotEmpty()){

                                    map[param] = files[0]

                                }
                            }
                            else -> {

                            }
                        }
                    }
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
                     val  params = processParams(function.parameters,req)
                            params[function.instanceParameter!!] = handler;
                            val res = function.callBy(params)
                    res as Response
                }

                addHandler(functionHandler, Method.POST, endpointAnnotation.path,handler.prefix)

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
                        addHandler(functionHandler, Method.GET, path,handler.prefix)
                    }
                    function.findAnnotation<Post>() != null -> {
                        val anon = function.findAnnotation<Post>()!!;
                        val path = anon.path
                        val mutipartFiles = anon.multipartFiles;
                        val mutipartFields = anon.multipartFields;
                        println("Found POST endpoint: $path on ${function.name}")
                        val functionHandler = { req: Request ->


                             val  params = processParams(function.parameters,req, multipartFields = mutipartFields, multipartFiles = mutipartFiles)
                            params[function.instanceParameter!!] = handler;
                            val res = function.callBy(params)
                            res as Response
                        }
                        addHandler(functionHandler, Method.POST, path,handler.prefix)
                    }
                    // Add support for other HTTP methods
                    function.findAnnotation<Put>() != null -> {
                        val path = function.findAnnotation<Put>()!!.path
                        println("Found PUT endpoint: $path on ${function.name}")
                        val functionHandler = { req: Request ->
                             val  params = processParams(function.parameters,req)
                            params[function.instanceParameter!!] = handler;
                            val res = function.callBy(params)
                            res as Response
                        }
                        addHandler(functionHandler, Method.PUT, path,handler.prefix)
                    }
                    function.findAnnotation<Delete>() != null -> {
                        val path = function.findAnnotation<Delete>()!!.path
                        println("Found DELETE endpoint: $path on ${function.name}")
                        val functionHandler = { req: Request ->
                             val  params = processParams(function.parameters,req)
                            params[function.instanceParameter!!] = handler;
                            val res = function.callBy(params)
                            res as Response
                        }
                        addHandler(functionHandler, Method.DELETE, path,handler.prefix)
                    }
                    function.findAnnotation<Patch>() != null -> {
                        val path = function.findAnnotation<Patch>()!!.path
                        println("Found PATCH endpoint: $path on ${function.name}")
                        val functionHandler = { req: Request ->
                             val  params = processParams(function.parameters,req)
                            params[function.instanceParameter!!] = handler;
                            val res = function.callBy(params)
                            res as Response
                        }
                        addHandler(functionHandler, Method.PATCH, path,handler.prefix)
                    }
                    function.findAnnotation<Head>() != null -> {
                        val path = function.findAnnotation<Head>()!!.path
                        println("Found HEAD endpoint: $path on ${function.name}")
                        val functionHandler = { req: Request ->
                             val  params = processParams(function.parameters,req)
                            params[function.instanceParameter!!] = handler;
                            val res = function.callBy(params)
                            res as Response
                        }
                        addHandler(functionHandler, Method.HEAD, path,handler.prefix)
                    }
                    function.findAnnotation<Options>() != null -> {
                        val path = function.findAnnotation<Options>()!!.path
                        println("Found OPTIONS endpoint: $path on ${function.name}")
                        val functionHandler = { req: Request ->
                             val  params = processParams(function.parameters,req)
                            params[function.instanceParameter!!] = handler;
                            val res = function.callBy(params)
                            res as Response
                        }
                        addHandler(functionHandler, Method.OPTIONS, path,handler.prefix)
                    }
                    function.findAnnotation<Trace>() != null -> {
                        val path = function.findAnnotation<Trace>()!!.path
                        println("Found TRACE endpoint: $path on ${function.name}")
                        val functionHandler = { req: Request ->
                             val  params = processParams(function.parameters,req)
                            params[function.instanceParameter!!] = handler;
                            val res = function.callBy(params)
                            res as Response
                        }
                        addHandler(functionHandler, Method.TRACE, path,handler.prefix)
                    }
                    function.findAnnotation<Purge>() != null -> {
                        val path = function.findAnnotation<Purge>()!!.path
                        println("Found Purge endpoint: $path on ${function.name}")
                        val functionHandler = { req: Request ->
                             val  params = processParams(function.parameters,req)
                            params[function.instanceParameter!!] = handler;
                            val res = function.callBy(params)
                            res as Response
                        }
                        addHandler(functionHandler, Method.PURGE, path,handler.prefix)
                    }
                }
            }
        }
    }



    private fun handleMultipartForm(request: Request, fields: Array<String>,files :Array<String>): List<UploadFile> {
        val multipartForm = MultipartFormBody.from(request)


        val list = mutableListOf<UploadFile>()


        files.forEach { field ->

            multipartForm.file(field)?.let { fileInput ->
                val up =  UploadFile(fileInput.filename, fileInput.contentType, fileInput.content);
                list.add(up)
            }

        }


        return list
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