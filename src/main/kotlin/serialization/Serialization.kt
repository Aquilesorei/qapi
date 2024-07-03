package org.aquiles.serialization

import com.google.gson.Gson
import org.aquiles.core.HttpResponse

import org.aquiles.core.jsonResponse

import kotlin.reflect.KType
import kotlin.reflect.javaType


fun handleResponse(res: Any?): HttpResponse {
    return when (res) {
        null -> HttpResponse.InternalServerError()
        is HttpResponse -> res
        is Number, is Boolean, is String, is Enum<*>,is Pair<*, *> -> HttpResponse.Ok().jsonResponse(res)
        is Collection<*> -> handleCollectionResponse(res)
        is Map<*, *> -> handleMapResponse(res)
        else -> handleCustomResponse(res)
    }
}

private fun handleCollectionResponse(res: Collection<*>): HttpResponse {
    return if (res.isEmpty() || res.first() is Number || res.first() is String || res.first() is Boolean || res.first() is Enum<*>) {
        HttpResponse.Ok().jsonResponse(res)
    } else {
        // Handle serialization for other collection types if needed
        HttpResponse.Ok().jsonResponse(res) // Placeholder
    }
}

private fun handleMapResponse(res: Map<*, *>):  HttpResponse {
    return if (res.isEmpty() || res.values.first() is Number || res.values.first() is String || res.values.first() is Boolean || res.values.first() is Enum<*>) {
        HttpResponse.Ok().jsonResponse(res)
    } else {
        // Handle serialization for other map types if needed
        HttpResponse.Ok().jsonResponse(res) // Placeholder
    }
}

private fun handleCustomResponse(res: Any):  HttpResponse{
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




/**
 *
 * Deserializes a json to the corresponding type
 */

@OptIn(ExperimentalStdlibApi::class)
fun fromJson(jsonMap: Map<String, Any>, kType: KType): Any? {
    try {
        val gson = Gson()
        val json = gson.toJson(jsonMap) // Convert map back to JSON string
        return gson.fromJson(json, kType.javaType)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
