package org.aquiles.core

import core.*
import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers
import io.undertow.util.NetworkUtils
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import org.apache.commons.fileupload.MultipartStream
import org.apache.commons.fileupload.MultipartStream.ProgressNotifier
import org.apache.commons.fileupload.util.Streams
import java.nio.charset.StandardCharsets

import java.net.InetSocketAddress
import java.util.*
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

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
    val  parts : List<MultipartPart> = listOf(),
    var source: RequestSource? = null,
    val  version : String = HTTP_1_1,

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

            var parts  : List<MultipartPart> = listOf()
            if (isMultipartRequest(exchange)) {
                // Check if multipart




                val contentTypeHeader = exchange.requestHeaders.getFirst(Headers.CONTENT_TYPE)
                val boundary = extractBoundary(contentTypeHeader)

                parts = parseMultipartData(boundary!!,exchange.inputStream);
                /*  println(parts.size)
                  request.multipartEntity = MultipartEntity(contentTypeHeader!!, boundary!!)
                      .parseForm(exchange)*/
            }
            val uri = exchange.requestURI;


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
                version = exchange.protocol.toString(),
                parts = parts
            );





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






fun parseMultipartData(boundary: String, inputStream: InputStream): List<MultipartPart> {
    val parts = mutableListOf<MultipartPart>()
    val boundaryBytes = boundary.toByteArray(StandardCharsets.UTF_8)
   // val notifier = ProgressNotifier()


    val multipartStream = MultipartStream(inputStream, boundaryBytes,8192,)

    var nextPart = multipartStream.skipPreamble()
    while (nextPart) {
        // Parse headers
        val headers = mutableMapOf<String, String>()
        val headerStream = multipartStream.readHeaders()
        val headerLines = headerStream.split("\r\n").filter { it.isNotBlank() }

        headerLines.forEach { headerLine ->
            val (key, value) = headerLine.split(": ", limit = 2)
            headers[key] = value
        }

        // Extract content disposition and part name
        val contentDisposition = headers["Content-Disposition"]
        val partName = contentDisposition?.substringAfter("name=\"")?.substringBefore("\"")

        // Handle file parts or text parts
        val contentDispositionIsFile = contentDisposition?.contains("filename=\"") == true
        if (contentDispositionIsFile) {
            val filename = contentDisposition?.substringAfter("filename=\"")?.substringBefore("\"")
            val contentType = headers["Content-Type"] ?: "application/octet-stream"


            // Use a buffer to handle large file efficiently

            val outputStream = ByteArrayOutputStream()
            multipartStream.readBodyData(outputStream)


            parts.add(
                MultipartFilePart(
                    headers,
                    contentDisposition,
                    partName ?: "",
                    filename ?: "",
                    contentType,
                    outputStream.toByteArray().inputStream()
                )
            )
        } else {
            // Handle text part
            val outputStream = ByteArrayOutputStream()
            multipartStream.readBodyData(outputStream)
            val partContent = String(outputStream.toByteArray(), StandardCharsets.UTF_8)

            parts.add(
                MultipartTextPart(
                    headers,
                    contentDisposition,
                    partContent,
                    partName ?: ""
                )
            )
        }

        nextPart = multipartStream.readBoundary()
    }

    return parts
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
