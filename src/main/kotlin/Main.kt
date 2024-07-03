package org.aquiles

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

import kotlinx.coroutines.delay
import Server.HttpServer
import  org.aquiles.core.HttpStatus
import org.aquiles.core.*

 val myFilter = HttpMiddleware { next: HttpHandler ->
    { request ->
        val start = System.currentTimeMillis()
        val response = next(request)
        val latency = System.currentTimeMillis() - start
        println("I took $latency ms")
        response
    }
}
data class  User(val name :String, val age :Int);
class MyScope : RoutingScope() {




    init {
        addScopeMiddleware(myFilter)
    }



    @Get("/blabla")
    fun blabal(): Int {
        return  42
    }


    @Get("/delay")
    suspend fun delayedResponse(): HttpResponse {
        delay(1000) // Simulate a delay
        return HttpResponse(HttpStatus.OK)//.body("Delayed response after 1 second")
    }

    @Post("/register")
    fun registerUser(user: User): HttpResponse {
        println("Registering user ${user.name}")

       return HttpResponse(HttpStatus.OK,"oulalalal\n")
    }


    @Get("/add/{a}/{b}")
    fun add(a: Int, b: Int):HttpResponse{
        if(a == 5) {
            throw Exception("they not like us")
        }
        return HttpResponse(HttpStatus.OK,"$a + $b = ${a + b}")
    }

    @Get("/hello/{lastname}/{name}")
    fun hello(lastname: String, name: String): HttpResponse{
        return HttpResponse(HttpStatus.OK)
    }

    @Post("/upload", multipartFiles = ["file", "file1"])
    fun upload(files: List<UploadFile>): HttpResponse {


        val builder = StringBuilder()
        for (f in files) {
            builder.append(f.fileName).append(" ")
            f.write("./upload/")
        }


        return HttpResponse(HttpStatus.OK,"received $builder")
    }





}



fun main() {


    val router = Router()
    router.addScope(MyScope(),prefix = "/api")
        .withRoutes(myRoutes)
        .staticFiles("/download", directory = "./upload")
        .start(9000)



}
