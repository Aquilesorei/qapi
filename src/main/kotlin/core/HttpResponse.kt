package org.aquiles.core


import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.http4k.core.Response


typealias HttpResponse = Response




/**
 * Sets the body of the response to a JSON representation of the data object,
 * using the provided Gson instance for serialization.
 * Returns a new Response object with the appropriate headers set.
 * Handles JsonSyntaxException by returning an INTERNAL_SERVER_ERROR response.
 */
fun <T> HttpResponse.jsonResponse(data: T, gson: Gson = Gson()): HttpResponse {
    return try {
        this.header("Content-Type", "application/json")
            .body(gson.toJson(data))
    } catch (e: JsonSyntaxException) {
        Response(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Error serializing response to JSON")
    }
}
/*
fun JsonResponse(data: Any,status : HttpStatus): HttpResponse {

}*/