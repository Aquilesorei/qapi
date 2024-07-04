package org.aquiles.core

import core.*
import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers
import io.undertow.util.NetworkUtils
import org.aquiles.Uri
import java.net.InetSocketAddress
import java.util.*


data class HttpRequest private  constructor(
    var status: Int,
    var path: String,
    override var headers: MutableMap<String, HeaderValue>,
    override var body: Body,
    val method: HttpMethod,
    val uri: Uri,
    val queryParameters: MutableMap<String, Deque<String>>,
    val pathParameters: MutableMap<String, Deque<String>>,
    var multipartEntity: MultipartEntity? = null,
    var source: RequestSource? = null,
    val  version : String = HTTP_1_1
) : HTTPMessage
{

    constructor(
        method: HttpMethod,
        uri :Uri,
        status: Int,
        content: String,
        path: String,
        queryParameters: MutableMap<String, Deque<String>>,
        pathParameters: MutableMap<String, Deque<String>>,
        source: RequestSource? = null,
        version : String = HTTP_1_1
    ) : this(
        version=version,
        source =source,
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




            var source : RequestSource? = null

          getHostAndPort(exchange)?.let { (host, port) ->
               source = RequestSource(
                   scheme =  exchange.requestScheme,
                   port = port,
                   address = host
               )
           }


            exchange.startBlocking()
            val uri = Uri.of(exchange.requestURI);
            val request = HttpRequest(
                queryParameters = exchange.queryParameters,
                pathParameters = exchange.pathParameters,
                path = exchange.requestPath,
                status =  exchange.statusCode,
                method = HttpMethod.fromString(exchange.requestMethod.toString()),
                uri =uri,
                body = Body(exchange.inputStream),
                headers = exchange.requestHeaders.toHeaderMap(),
                source = source,
                version = exchange.protocol.toString()
            );




            if (isMultipartRequest(exchange)) { // Check if multipart
                val contentTypeHeader = exchange.requestHeaders.getFirst(Headers.CONTENT_TYPE)
                val boundary = extractBoundary(contentTypeHeader)

                request.multipartEntity = MultipartEntity(contentTypeHeader!!, boundary!!)
                    .parseForm(exchange)
            }
            return request
        }


        //exchange.requestURI
        private fun extractBoundary(contentTypeHeader: String?): String? {
            contentTypeHeader ?: return null // If header is null, there's no boundary

            val boundaryMatch = Regex("boundary=(?<boundary>[-a-zA-Z0-9']+)").find(contentTypeHeader)
            return boundaryMatch?.groups?.get("boundary")?.value?.trim()?.removeSurrounding("\"")
        }

        private fun isMultipartRequest(exchange: HttpServerExchange): Boolean {
            val contentType = exchange.requestHeaders.getFirst(Headers.CONTENT_TYPE)
            return contentType?.startsWith("multipart/") ?: false
        }


        const val HTTP_1_1 = "HTTP/1.1"
        const val HTTP_2 = "HTTP/2"


    }
}


data class RequestSource(val address: String, val port: Int? = 0, val scheme: String? = null)


private fun getHostAndPort(exchange: HttpServerExchange): Pair<String, Int>? {
    val hostHeader = exchange.requestHeaders.getFirst(Headers.HOST)?.trim()

    return if (hostHeader.isNullOrBlank()) {
        getSocketAddressHostAndPort(exchange)
    } else {
        parseHostHeader(hostHeader)
    }
}

private fun getSocketAddressHostAndPort(exchange: HttpServerExchange): Pair<String, Int>? {
    val address = exchange.destinationAddress as? InetSocketAddress ?: return null
    val host = NetworkUtils.formatPossibleIpv6Address(address.hostString)
    val port = address.port

    return if ((exchange.requestScheme == "http" && port != 80) ||
        (exchange.requestScheme == "https" && port != 443)) {
        host to port
    } else {
        host to -1 // Indicate default port (to be omitted)
    }
}

private fun parseHostHeader(hostHeader: String): Pair<String, Int> {
    val parts = hostHeader.split(":")
    return if (parts.size == 2 && parts[1].toIntOrNull() != null) {
        parts[0] to parts[1].toInt()
    } else {
        hostHeader to -1 // Indicate default port
    }
}
