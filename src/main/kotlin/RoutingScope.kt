package org.aquiles

import org.aquiles.core.HttpMiddleware

abstract  class RoutingScope() {

    private val scopeMiddleware = mutableListOf<HttpMiddleware>()
    private val routeMiddleware = mutableMapOf<String, MutableList<HttpMiddleware>>()

   open fun addScopeMiddleware(vararg middleware: HttpMiddleware) {
        scopeMiddleware.addAll(middleware)
    }

  open  fun addRouteMiddleware(path: String, vararg middleware: HttpMiddleware){
        routeMiddleware.computeIfAbsent(path) { mutableListOf() }.addAll(middleware)
    }



 open fun scopeMiddleware(): Array<HttpMiddleware>{
     return  scopeMiddleware.toTypedArray()
 }



   open fun middleware(): Map<String ,List<HttpMiddleware>>{
       return  routeMiddleware;
   }

}