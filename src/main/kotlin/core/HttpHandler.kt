package org.aquiles.core



typealias  HttpHandler = (HttpRequest) -> HttpResponse



fun interface HttpMiddleware : (HttpHandler) -> HttpHandler {
    companion object
}

val HttpMiddleware.Companion.NoOp: HttpMiddleware get() = HttpMiddleware { it }

fun HttpMiddleware.then(next: HttpMiddleware): HttpMiddleware = HttpMiddleware { this(next(it)) }

fun HttpMiddleware.then(next: HttpHandler): HttpHandler = this(next)

//fun Filter.then(next: RoutingHttpHandler): RoutingHttpHandler = next.withFilter(this)
