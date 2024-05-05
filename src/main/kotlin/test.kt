package org.aquiles

import org.http4k.core.*
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.ServerFilters


fun  test(){
    val handler = { _: Request -> Response(OK) }

    val myFilter = Filter {
            next: HttpHandler -> {
            request: Request ->
        val start = System.currentTimeMillis()
        val response = next(request)
        val latency = System.currentTimeMillis() - start
        println("I took $latency ms")
        response
    }
    }
    val latencyAndBasicAuth: Filter = ServerFilters.BasicAuth("my realm", "user", "password").then(myFilter)
    val app: HttpHandler = latencyAndBasicAuth.then(handler)
}