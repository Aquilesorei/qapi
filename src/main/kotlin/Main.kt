package org.aquiles

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import org.http4k.core.Status.Companion.OK

import org.http4k.core.*
import org.http4k.routing.path
class RoutingScope {
    @EndPoint(Method.GET, "/add")
    fun add(request: Request): Response {
       return Response(OK).body("addddddingkkgk")
    }




}
fun main() {
    // Define some routes

    val routes = arrayOf(
        Route.get("/hello/{name}") { req ->
            val name = req.path("name") ?: "World"
            Response(OK).body("Hello, $name!")
        },
        Route.post( "/echo") { req ->
            Response(OK).body(req.bodyString())
        }
    )

    // Create a router with the defined routes
    val router = Router(*routes)

    router.addAnnotatedHandler(RoutingScope())

    // Start the server
  router.start(9000)

}
