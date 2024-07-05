package openapi

import core.RouteData


class OpenAPIGenerator(private val routes: List<RouteData>) {

    fun generateOpenAPISpec(info: Info): OpenAPISpec {
        val paths = mutableMapOf<String, PathItem>()
        val components = Components()

        routes.forEach { route ->
            val pathItem = paths.computeIfAbsent(route.path) { PathItem() }
            val operation = createOperation(route)
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

        return OpenAPISpec(info = info, paths = paths, components = components)
    }

    private fun createOperation(route: RouteData): Operation {
        return Operation(
            summary = "Operation for ${route.path}",
            operationId = route.path.trim('/').replace("/", "_"),
            tags = listOf("API"),
            responses = mapOf(
                "200" to Response(description = "Success"),
                "400" to Response(description = "Bad Request"),
                "404" to Response(description = "Not Found")
            )
        )
    }
}
