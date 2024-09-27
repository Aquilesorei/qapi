
# QAPI

## Overview

QAPI is a custom-built HTTP library designed to facilitate the development of web servers with Kotlin. It provides a simple and flexible way to handle HTTP requests and responses, supports middleware for request processing, and offers various features for routing and file handling.

## Features

- **Middleware Support**: Easily add middleware to process requests and responses.
- **Routing**: Define routes and handle different HTTP methods (`GET`, `POST`, etc.).
- **Delay Simulation**: Simulate delays to test asynchronous behavior.
- **File Upload Handling**: Handle file uploads with support for multiple files.
- **OpenAPI Specification**: Generate and print an OpenAPI specification for your routes.

## Getting Started

### Installation

Add the necessary dependencies to your project. (Include instructions here based on your build system, e.g., Gradle or Maven.)

### Usage

1. **Define Middleware**: Create middleware to process requests and responses.

    ```kotlin
    val myFilter = HttpMiddleware { next: HttpHandler ->
        { request ->
            val start = System.currentTimeMillis()
            val response = next(request)
            val latency = System.currentTimeMillis() - start
            println("I took $latency ms")
            response
        }
    }
    ```

2. **Create Routing Scope**: Define routes and request handlers.

    ```kotlin
    data class User(val name: String, val age: Int)

    class MyScope : RoutingScope() {

        init {
            addScopeMiddleware(myFilter)
        }

        @Get("/blabla")
        fun blabal(): Int {
            return 42
        }

        @Get("/test")
        fun test(): String {
            return "test"
        }

        @Get("/delay")
        suspend fun delayedResponse(): HttpResponse {
            delay(1000) // Simulate a delay
            return HttpResponse(HttpStatus.OK)
        }

        @Post("/register")
        fun registerUser(user: User): HttpResponse {
            println("Registering user ${user.name}")
            return HttpResponse(HttpStatus.OK, "oulalalal\n")
        }

        @Get("/add/{a}/{b}")
        fun add(a: Int, b: Int): HttpResponse {
            return HttpResponse(HttpStatus.OK, "$a + $b = ${a + b}")
        }

        @Get("/hello/{lastname}/{name}")
        fun hello(lastname: String, name: String): HttpResponse {
            return HttpResponse(HttpStatus.OK, "Hello $lastname, $name", contentType = ContentType.TEXT_PLAIN)
        }

        @Post("/upload", multipartFiles = ["file", "file1"])
        fun upload(files: List<UploadFile>): HttpResponse {
            val builder = StringBuilder()
            for (f in files) {
                builder.append(f.fileName).append(" ")
                f.write("./upload/")
            }
            return HttpResponse(HttpStatus.OK, "received $builder")
        }
    }
    ```

3. **Set Up Router**: Configure the router and start the server.

    ```kotlin
    fun main() {
        val router = Router()
        router.addScope(MyScope(), prefix = "/api")
            .withRoutes(myRoutes)
            .staticFiles("/download", directory = "./upload")

        router.printOpenAPISpec()
    }
    ```

## License

MIT
## Contributing

If you'd like to contribute to this project, please fork the repository and submit a pull request with your changes. Make sure to follow the contribution guidelines.

## Contact

For any questions or feedback, please contact [achillezongo07@gmail.com](mailto:achillezongo07@gmail.com).

