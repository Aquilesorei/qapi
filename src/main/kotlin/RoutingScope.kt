package org.aquiles

open class RoutingScope {
    open fun  toRouter(): Router {

        val router = Router()
        router.addAnnotatedHandler(this)
        return router
    }
}