package org.aquiles.core

import core.*
import io.undertow.server.HttpServerExchange
import io.undertow.util.BadRequestException
import io.undertow.util.Headers
import org.aquiles.toMutableMap
import java.util.*


data class HttpRequest private  constructor(
    var status: Int,
    var path: String,
    override var headers: MutableMap<String, HeaderValue>,
    override var body: Body,
    val method: HttpMethod,
    val uri: String,
    val queryParameters: MutableMap<String, Deque<String>>,
    val pathParameters: MutableMap<String, Deque<String>>,
    var multipartEntity: MultipartEntity? = null,
) : HTTPMessage
{

    constructor(
        method: HttpMethod,
        uri :String,
        status: Int,
        content: String,
        path: String,
        queryParameters: MutableMap<String, Deque<String>>,
        pathParameters: MutableMap<String, Deque<String>>,
    ) : this(
        queryParameters= queryParameters,
        pathParameters= pathParameters,
        status =status,
        headers = mutableMapOf(),body = Body(content), uri = uri,method =method,path = path)




    fun query(q : String) : String? {
        return queryParameters[q]?.firstOrNull()
    }

    fun path(p : String) : String? {
        return pathParameters[p]?.firstOrNull()
    }



    override fun addHeader(name: String, value: String) {
        headers[name] = HeaderValue(value)
    }

    override fun getHeader(name: String): String? = headers[name]?.firstDirective
    override fun getHeaderDirectives(name: String): Map<String, String?>? = headers[name]?.parameters?.toMap()

    override fun removeHeader(name: String) {
        headers.remove(name)
    }


    companion object{
        fun from(exchange: HttpServerExchange): HttpRequest {

            fun extractBoundary(contentTypeHeader: String?): String? {
                contentTypeHeader ?: return null // If header is null, there's no boundary

                val boundaryMatch = Regex("boundary=(?<boundary>[-a-zA-Z0-9']+)").find(contentTypeHeader)
                return boundaryMatch?.groups?.get("boundary")?.value?.trim()?.removeSurrounding("\"")
            }

            fun isMultipartRequest(exchange: HttpServerExchange): Boolean {
                val contentType = exchange.requestHeaders.getFirst(Headers.CONTENT_TYPE)
                return contentType?.startsWith("multipart/") ?: false
            }
            exchange.startBlocking()
            val request = HttpRequest(
                queryParameters = exchange.queryParameters,
                pathParameters = exchange.pathParameters,
                path = exchange.requestPath,
                status =  exchange.statusCode,
                method = HttpMethod.fromString(exchange.requestMethod.toString()),
                uri = exchange.requestURI,
                body = Body(exchange.inputStream),
                headers = exchange.requestHeaders.toHeaderMap()

            );

            if (isMultipartRequest(exchange)) { // Check if multipart
                val contentTypeHeader = exchange.requestHeaders.getFirst(Headers.CONTENT_TYPE)
                val boundary = extractBoundary(contentTypeHeader)

                request.multipartEntity = MultipartEntity(contentTypeHeader!!, boundary!!)
                    .parseForm(exchange)
            }
            return request
        }




    }
}


