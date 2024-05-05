package org.aquiles

abstract  class RoutingScope(val prefix : String? =null) {
    open fun  toRouter(): Router {

        val router = Router()
        router.addAnnotatedHandler(this)
        return router
    }


}