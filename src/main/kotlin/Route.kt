package org.aquiles

import org.http4k.core.Method
import org.http4k.core.Method.*
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind

class Route(
    private val method: Method,
    private val path: String,
    private val handler: (Request) -> Response
){

   fun toHandler(): RoutingHttpHandler {
      return  path bind method to handler
    }

    override fun toString() = "$method $path"

    companion object {
        fun get(path: String, handler: (Request) -> Response) = Route(GET, path, handler)
        fun post(path: String, handler: (Request) -> Response) = Route(POST, path, handler)
        fun put(path: String, handler: (Request) -> Response) = Route(PUT, path, handler)
        fun delete(path: String, handler: (Request) -> Response) = Route(DELETE, path, handler)
        fun head(path: String, handler: (Request) -> Response) = Route(HEAD, path, handler)
        fun options(path: String, handler: (Request) -> Response) = Route(OPTIONS, path, handler)
        fun patch(path: String, handler: (Request) -> Response) = Route(PATCH, path, handler)
        fun trace(path: String, handler: (Request) -> Response) = Route(TRACE, path, handler)
        fun purge(path: String, handler: (Request) -> Response) = Route(PURGE, path, handler)
    }
}