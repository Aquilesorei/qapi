package openapi

import core.RouteData
import org.aquiles.convertToSentenceCase
import org.aquiles.core.HttpMethod
import kotlin.reflect.KCallable


object OpenAPIGenerator {
    private val paths = mutableMapOf<String, PathItem>()
    private val components = Components()

        fun generateOpenAPISpec(
            info: Info,
            servers : List<OpenApiServer> = listOf(),
            host : String,
            basePath : String? = null, //e.g /v2
            schemes  : List<String> = listOf(),
            ): OpenAPISpec {

            return OpenAPISpec(info = info, paths = paths, components = components, servers = servers, host = host, schemes = schemes, basePath = basePath)
        }


    fun addPathItem(block: (MutableMap<String, PathItem>) -> Unit) {

        block(paths)
    }
    fun addPathItem(route: RouteData, function : KCallable<*>, method : HttpMethod,) {
        val pathItem = paths.computeIfAbsent(route.path) { PathItem() }
        val properties = mutableMapOf<String, Property>()
        val parameters = mutableListOf<Parameter>()
        val required = mutableListOf<String>()
        var body : RequestBody? = null

        function.parameters.forEach { param ->

            param.name?.let {
                if (!param.isOptional) {
                    required.add(it) // add it hahahhah
                }

                val  prname = getOpenApiPrimitiveType(param.type)


                if(param.type.isPrimitive()) {

                    val constraints = mutableMapOf<String, Any>()
                    val  format = getOpenApiFormat(param.type)
                    format?.let { constraints["format"] = format }

                    parameters.add(
                        Parameter(
                            name = it,
                            description = "",
                            `in` = if(route.path.contains(it)) "path" else "query",
                            required = !param.isOptional,
                            schema = OpenApiPrimitiveFactory.create(
                                type = prname,
                                constraints = constraints
                            )
                        ))
                }else if(param.type.isDataClass()){

                    if(method == HttpMethod.PUT || method == HttpMethod.POST){
                        body =  RequestBody(
                            content = mapOf(
                                "application/json" to MediaType(
                                    schema = Schema(
                                        properties=properties,
                                        required = required,
                                        description = "example value",
                                        example = createDummyInstance(param.type)
                                    ),
                                )
                            )
                        )
                    }
                }
                else if(prname != "undefined"){
                    properties[it] =   Property(
                        type = getOpenApiPrimitiveType(param.type),
                        required = !param.isOptional,
                        readOnly =  true,
                        example = createDummyInstance(param.type)
                    )
                }

            }
        }




        var operation = Operation.sample(
        summary = convertToSentenceCase(function.name),
        operationId = route.path.trim('/').replace("/", "_"),
        )

        // todo : get the parameters from the underlying
        operation = operation.copy(

            parameters = parameters,
           requestBody = body
        )


        when (route.method) {
            "GET" -> pathItem.copy(get = operation)
            "POST" -> pathItem.copy(post = operation)
            "PUT" -> pathItem.copy(put = operation)
            "DELETE" -> pathItem.copy(delete = operation)
            "PATCH" -> pathItem.copy(patch = operation)
            "HEAD" -> pathItem.copy(head = operation)
            "OPTIONS" -> pathItem.copy(options = operation)
            "TRACE" -> pathItem.copy(trace = operation)
            else -> pathItem
        }.also { paths[route.path] = it }

    }

}


