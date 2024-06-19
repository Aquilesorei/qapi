package org.aquiles

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import io.undertow.util.BadRequestException
import org.http4k.core.Status.Companion.OK

import org.http4k.core.*
import org.http4k.routing.path

class MyScope(prefix: String? = null) : RoutingScope(prefix) {
    private val myFilter = HttpMiddleware {
            next: HttpHandler -> {
            request: Request ->
        val start = System.currentTimeMillis()
        val response = next(request)
        val latency = System.currentTimeMillis() - start
        println("I took $latency ms")
        response
    }
    }



    init {
        addScopeMiddleware(myFilter)
    }


    @Get("/add/{a}/{b}")
    fun add(a: Int, b: Int): Response {
        if(a == 5) {
            throw Exception("they not like us")
        }
        return Response(OK).body("$a + $b = ${a + b}")
    }

    @Get("/hello/{lastname}/{name}")
    fun hello(lastname: String, name: String): Response {
        return Response(OK).body("Hello, $name you are $lastname")
    }

    @Post("/upload", multipartFiles = ["file", "file1"])
    fun upload(files: List<UploadFile>): Response {


        val builder = StringBuilder();
        for (f in files) {
            builder.append(f.fileName).append(" ")
            f.write("./upload/")
        }

        return Response(OK).body("received $builder")
    }





}



fun main() {
    val router = Router()
    router.addScope(MyScope(prefix = "/api"))
    router.staticFiles("/download", directory = "./upload")
    router.start(9000)
}
