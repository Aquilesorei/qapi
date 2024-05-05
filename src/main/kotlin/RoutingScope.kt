package org.aquiles

abstract  class RoutingScope(val prefix : String? =null) {

 open fun scopeMiddleware(): Array<HttpMiddleware>{
     return  emptyArray()
 }

   open fun middleware(): Map<String ,Array<HttpMiddleware>>{

       return  emptyMap()
   }
    open fun  toRouter(): Router {

        val router = Router()
        router.addScope(this)
        return router
    }

}