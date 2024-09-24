package openapi



typealias Name = String
data class OpenAPISpec(
    val openapi: String = "3.0.1",
    val info: Info,
    val servers : List<OpenApiServer> = listOf(),
    val paths: Map<String, PathItem> = emptyMap(),
    val components: Components = Components(),
    val  host : String,
    val basePath : String? = null,
    val schemes  : List<String> = listOf(),
)

data class  OpenApiServer(
    val url : String,
    val description : String,
)

data class Info(
    val version: String,
    val title: String
)


data class PathItem(
    val get: Operation? = null,
    val post: Operation? = null,
    val put: Operation? = null,
    val delete: Operation? = null,
    val patch: Operation? = null,
    val head: Operation? = null,
    val options: Operation? = null,
    val trace: Operation? = null
)


data class Operation(
    val summary: String,
    val operationId: String,
    val tags: List<String> = emptyList(),
    val parameters: List<Parameter> = emptyList(),
    val requestBody: RequestBody? = null, // generally for post ,put ...
    val responses: Map<String, Response>
){


    companion object{
         fun  sample(summary: String , operationId: String): Operation {
             return    Operation(
                 summary = summary,
                 operationId =  operationId,
                 tags = listOf("API"),
                 responses = mapOf(
                     "200" to Response(description = "Success",
                         content = mapOf(
                         "application/json" to  MediaType(schema = Schema(example = "string", description = "example value")),
                     )),
                     "400" to Response(description = "Bad Request",),
                     "404" to Response(description = "Not Found")
                 ))

         }
    }
}

/**
 * for query and path params
 */
data class Parameter(
    val name: String,
    val `in`: String,
    val description: String,
    val required: Boolean,
    val schema: OpenApiPrimitive
)

/**
 *
 * Represent the data that  is  being  sent using post or put   by client
 * @param content   :  a map of content-type to  the  schema
 */
data class RequestBody(
    val content: Map<String, MediaType>
)


data class MediaType(
    val schema: Schema
)


data class Response(
    val description: String,
    val content: Map<String, MediaType>? = null // the string is the media type ,the rest is the schema
)


data class Components(
    val schemas: Map<String, Schema> = emptyMap(),
    val parameters: Map<String, Parameter> = emptyMap(),
    val responses: Map<String, Response> = emptyMap()
)



data class Schema(
    val type: String?=null,
    val items: Schema? = null,
    val properties: Map<Name, Property>? = null,
    val required: List<String>? = null,
    val description: String? = null,
    val example: Any? = null
)



data class Property(
    val type: String,
    val format: String? = null,
    val required: Boolean? = null,
    val description: String? = null,
    val  readOnly: Boolean? = null,
    val example: Any? = null,
    val  maxLength : Int? = null,
   val items : Schema? = null,  // uses for list  when type == "array"
)