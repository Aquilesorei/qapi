package org.aquiles

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import org.http4k.core.Status.Companion.OK

import org.http4k.core.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MyScope(prefix: String? = null) : RoutingScope(prefix) {

    @Get("/add/{a}/{b}")
    fun add(a: Int, b: Int): Response {
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

    override fun scopeMiddleware(): Array<HttpMiddleware> {
        return arrayOf(
       myFilter
        )
    }

    override fun middleware(): Map<String, Array<HttpMiddleware>> {

        return mapOf(

        )
    }



}



fun main() {
    val router = Router()
    router.addScope(MyScope(prefix = "/api"))


    router.start(9000)
}
