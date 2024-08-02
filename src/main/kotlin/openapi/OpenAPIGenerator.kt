package openapi

import core.RouteData


object OpenAPIGenerator {
    val paths = mutableMapOf<String, PathItem>()
    val components = Components()

    /*
        fun generateOpenAPISpec(info: Info): OpenAPISpec {


            routes.forEach { route ->
                addPathItem(route)
            }

            return OpenAPISpec(info = info, paths = paths, components = components)
        }
    */


    fun addPathItem(block: (MutableMap<String, PathItem>) -> Unit) {

        block(paths)
    }
    fun addPathItem(route: RouteData ) {
        val pathItem = paths.computeIfAbsent(route.path) { PathItem() }
        val operation = Operation.sample(
        summary = "Operation for ${route.path}",
        operationId = route.path.trim('/').replace("/", "_"),
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


