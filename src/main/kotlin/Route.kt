package org.aquiles

import core.RouteData
import org.aquiles.core.*
import org.aquiles.core.HttpMethod.*

class Route(
    val method: HttpMethod,
    val path: String,
    val handler:HttpHandler,
    private val  middlewares : List<HttpMiddleware> = emptyList(),
    val childRoutes : List<Route> = emptyList()
){




    fun toHandler(): List<RouteData> {
        val initialRouteData = RouteData(method.name, normalizePath(path), handler)
        val allRoutes = mutableListOf(initialRouteData)

        // Recursively collect all child routes
        fun collectRoutes(route: Route, parentPath: String) {
            for (child in route.childRoutes) {
                val fullPath =
                   normalizePath(parentPath + child.path)
                allRoutes.add(RouteData(child.method.name, fullPath, child.handler))
                collectRoutes(child, fullPath)
            }
        }

        collectRoutes(this, path)

        // Apply middlewares to each collected route
        return allRoutes.onEach { routeData ->
            middlewares.forEach { middleware ->
                routeData.handler = middleware.then(routeData.handler)
            }
        }
    }






    override fun toString() = "$method $path"

    companion object {
        fun get(path: String, handler: HttpHandler)= Route(GET, path, handler)
        fun post(path: String, handler: HttpHandler)= Route(POST, path, handler)
        fun put(path: String, handler: HttpHandler)= Route(PUT, path, handler)
        fun delete(path: String, handler: HttpHandler)= Route(DELETE, path, handler)
        fun head(path: String, handler: HttpHandler)= Route(HEAD, path, handler)
        fun options(path: String, handler: HttpHandler)= Route(OPTIONS, path, handler)
        fun patch(path: String, handler: HttpHandler)= Route(PATCH, path, handler)
        fun trace(path: String, handler: HttpHandler)= Route(TRACE, path, handler)
        fun purge(path: String, handler: HttpHandler)= Route(PURGE, path, handler)


        fun withChild(
            method: HttpMethod,
            path: String,
            handler: HttpHandler,
            middlewares: List<HttpMiddleware> = emptyList(),
            childRoutes: List<Route>
        ): Route {


          return  Route(
                method = method,
                path = path,
                childRoutes = childRoutes,
                middlewares = middlewares,
                handler = handler
            );

        }
    }
}







val  myRoutes = listOf(
    Route(
        method = GET,
        path = "/",
        middlewares = listOf(),
        handler = {req : HttpRequest->

            return@Route    HttpResponse(HttpStatus.OK,"Hello World!")
        },
        childRoutes = listOf(
            Route(
                method = GET,
                path = "/wtf",
                childRoutes = listOf(
                    Route(
                        method = GET,
                        path = "/wtf2",
                        childRoutes = listOf(),
                        middlewares = listOf(),
                        handler = {req : HttpRequest->


                            return@Route  HttpResponse(HttpStatus.OK,"Hello World subroute 2")
                        }
                    ),
                ),
                middlewares = listOf(),
                handler = {req :  HttpRequest->

                    return@Route   HttpResponse(HttpStatus.OK,"Hello World subroute 1")
                }
            ),

            Route(
                method = GET,
                path = "/getUser",
                childRoutes = listOf(),
                middlewares = listOf(
                    myFilter
                ),
                handler = {req : HttpRequest->

                    return@Route  HttpResponse.Ok().jsonResponse(User("Achille",23))
                }
            ),
        ),
    ),
    Route(
        method = GET,
        path = "/hello",
        childRoutes = listOf(),
        middlewares = listOf(),
        handler = {req : HttpRequest->

            return@Route    HttpResponse(HttpStatus.OK,"Hello World 2")
        }
    ),
    Route(
        method = GET,
        path = "/calculate",
        childRoutes = listOf(),
        middlewares = listOf(),
        handler = {req : HttpRequest->

            return@Route    HttpResponse(HttpStatus.OK,"Hello World 3")
        }
    ),
)