package org.aquiles.serialization

import org.aquiles.core.HttpResponse
import org.aquiles.core.jsonResponse

import org.http4k.core.Response




fun handleResponse(res: Any?): Response {
    return when (res) {
        null -> HttpResponse.InternalServerError().body("Internal server error")
        is Response -> res
        is Number, is Boolean, is String, is Enum<*>,is Pair<*, *> -> HttpResponse.Ok().jsonResponse(res)
        is Collection<*> -> handleCollectionResponse(res)
        is Map<*, *> -> handleMapResponse(res)
        else -> handleCustomResponse(res)
    }
}

private fun handleCollectionResponse(res: Collection<*>): Response {
    return if (res.isEmpty() || res.first() is Number || res.first() is String || res.first() is Boolean || res.first() is Enum<*>) {
        HttpResponse.Ok().jsonResponse(res)
    } else {
        // Handle serialization for other collection types if needed
        HttpResponse.Ok().jsonResponse(res) // Placeholder
    }
}

private fun handleMapResponse(res: Map<*, *>): Response {
    return if (res.isEmpty() || res.values.first() is Number || res.values.first() is String || res.values.first() is Boolean || res.values.first() is Enum<*>) {
        HttpResponse.Ok().jsonResponse(res)
    } else {
        // Handle serialization for other map types if needed
        HttpResponse.Ok().jsonResponse(res) // Placeholder
    }
}

private fun handleCustomResponse(res: Any): Response {
    val kClass = res::class
    return when {
        kClass.isData || kClass.isSealed || kClass.isAbstract || kClass.objectInstance != null -> {
            // Handle custom class serialization
            HttpResponse.Ok().jsonResponse(res) // Placeholder
        }
        else -> {
            // Handle other types if needed
            HttpResponse.Ok().jsonResponse(res) // Placeholder
        }
    }
}
