package org.aquiles

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Method.*
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities.Private

class Route(
    private val method: Method,
    private val path: String,
    private val handler:HttpHandler,
    private val  middlewares : List<HttpMiddleware> = emptyList(),
    private  val childRoutes : List<Route> = emptyList()
){

   fun toHandler(): RoutingHttpHandler {



       val newList =  emptyList<RoutingHttpHandler>().toMutableList();
       val finalHandler =   "/" bind method to handler
       newList.add(finalHandler)
       newList.addAll(childRoutes.map { it.toHandler() })

        return path bind routes(newList)



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
            method: Method,
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
         /*   var  h = path bind  method to handler;

            lateinit var  finalHandler  : HttpHandler

            val  list = emptyList<HttpHandler>().toMutableList()
           for(ch in  childRoutes){
               list.add(h bind  ch.toHandler())
           }


           return routes(list);*/
        }
    }
}







val  myRoutes = listOf(
    Route(
        method = GET,
        path = "/",
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
                        handler = {req : Request->

                            return@Route  Response(OK).body("Hello World subroute 2")
                        }
                    ),
                ),
                middlewares = listOf(),
                handler = {req : Request->

                    return@Route  Response(OK).body("Hello World subroute 1")
                }
            ),

            Route(
                method = GET,
                path = "/wtf2",
                childRoutes = listOf(),
                middlewares = listOf(),
                handler = {req : Request->

                    return@Route  Response(OK).body("Hello World subroute 2")
                }
            ),
        ),
        middlewares = listOf(

        ),
        handler = {req : Request->

            return@Route  Response(OK).body("Hello World!")
        }
    ),
    Route(
        method = GET,
        path = "/hello",
        childRoutes = listOf(),
        middlewares = listOf(),
        handler = {req : Request->

            return@Route  Response(OK).body("Hello World 2")
        }
    ),
    Route(
        method = GET,
        path = "/calculate",
        childRoutes = listOf(),
        middlewares = listOf(),
        handler = {req : Request->

            return@Route  Response(OK).body("Hello World 3")
        }
    ),
)