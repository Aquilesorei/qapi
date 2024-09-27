package org.aquiles

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

import MyPostsScope
import kotlinx.coroutines.delay
import Server.HttpServer
import core.QSslConfig
import io.undertow.Handlers.resource
import io.undertow.Undertow
import io.undertow.server.handlers.resource.PathResourceManager
import  org.aquiles.core.HttpStatus
import org.aquiles.core.*
import java.nio.file.Paths

internal val myFilter = HttpMiddleware { next: HttpHandler ->
    { request ->
        val start = System.currentTimeMillis()
        val response = next(request)
        val latency = System.currentTimeMillis() - start
        println("I took $latency ms")
        response
    }
}
internal data class  User(val name :String, val age :Int);
internal class MyScope : RoutingScope() {




    init {
        addScopeMiddleware(myFilter)
    }



    @Get("/blabla")
    fun blabal(): Int {
        return  42
    }

    @Get("/test")
    fun test(): String {
        return  "test"
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
        return HttpResponse(status = HttpStatus.OK, content = "$a + $b = ${a + b}")
    }

    @Get("/hello/{lastname}/{name}")
    fun hello(lastname: String, name: String): HttpResponse{
        return HttpResponse(HttpStatus.OK , content = "Hello $lastname, $name", contentType = ContentType.TEXT_PLAIN)
    }


    @Post("/upload", multipartFiles = ["file"])
    fun upload(file: UploadFile): String {

            file.write("./upload/")


        return "received ${file.fileName}"
    }

/*    @Post("/upload", multipartFiles = ["file", "file1"])
    fun upload(files: List<UploadFile>): String {


        val builder = StringBuilder()
        for (f in files) {
            builder.append(f.fileName).append(" ")
            f.write("./upload/")
        }


        return "received $builder"
    }*/





}



fun main() {


    val router = Router()
    router.addScope(MyPostsScope(),prefix = "/api")
        .withRoutes(myRoutes)
        .staticFiles( path = "download", directory = "./upload")
        .start(9000,
/*sslConfig = QSslConfig(
           keyStorePath = "./keystore.jks",
            keyStorePassword = "passwordd",
            keyPassword = "passwordd"
        )*/

        )




}
