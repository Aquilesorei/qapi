package org.aquiles

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import org.http4k.core.Status.Companion.OK

import org.http4k.core.*
import org.http4k.routing.path
class MyScope : RoutingScope() {
    @Get( "/add")
    fun add(request: Request): Response {
       return Response(OK).body("addddddingkkgk")
    }

   @Get( "/hello/{name}")
   fun hello(request: Request): Response {
       return Response(OK).body("Hello, ${request.path("name")}")
   }

  @Post("/echo")
  fun echo(request: Request): Response {
    return Response(OK).body(request.bodyString())
  }


}
fun main() {
    // Define some routes

    // Create a router with the defined routes
   // val router = Router()

   // router.addAnnotatedHandler(MyScope())

    // Start the server
   MyScope().toRouter().start(9000)

}
