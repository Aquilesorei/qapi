package org.aquiles.core


import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import core.Body
import core.HTTPMessage
import core.HeaderValue

import java.io.InputStream
import java.nio.ByteBuffer


class HttpResponse private  constructor(var statusCode: HttpStatus, override var body: Body, var contentType: ContentType) :
    HTTPMessage {
    override val headers: MutableMap<String, HeaderValue> = mutableMapOf()

    constructor( status: HttpStatus ,content: String , contentType:ContentType =  ContentType.TEXT_PLAIN) : this(status,body = Body(content), contentType)
    constructor(status:  HttpStatus, content: InputStream, ) : this(status,body = Body(content), contentType = ContentType.OCTET_STREAM)
    constructor(status: HttpStatus)  : this(status, Body.EMPTY,contentType = ContentType.TEXT_PLAIN)




    init {
        addHeader("Content-Type", contentType.toHeaderValue())
    }


    fun body(body : String) {
        this.body = Body(body)
    }
    fun body(body : ByteBuffer){
        this.body = Body(body)
    }

    fun body(body : InputStream) {
        this.body = Body(body)

    }
    fun body(body : Body) {
        this.body = body
    }






    override fun addHeader(name: String, value: String) {
        headers[name] = HeaderValue(value)
    }

    override fun getHeader(name: String): String? = headers[name]?.firstDirective
    override fun getHeaderDirectives(name: String): Map<String, String?>? = headers[name]?.parameters?.toMap()

    override fun removeHeader(name: String) {
        headers.remove(name)
    }




    companion object {

        fun json(json : String): HttpResponse {
            return HttpResponse(status =HttpStatus.OK, content =json,contentType = ContentType.APPLICATION_JSON)
        }

        fun  Continue(): HttpResponse {
            return HttpResponse(HttpStatus.CONTINUE)
        }
        fun SwitchingProtocols(): HttpResponse {
            return HttpResponse(HttpStatus.SWITCHING_PROTOCOLS)
        }
        fun Processing(): HttpResponse {
            return HttpResponse(HttpStatus(102, "Processing"))
        }

        fun Ok(): HttpResponse {
            return HttpResponse(HttpStatus.OK)
        }
        fun Created(): HttpResponse {
            return HttpResponse(HttpStatus.CREATED)
        }
        fun Accepted(): HttpResponse {
            return HttpResponse(HttpStatus.ACCEPTED)
        }
        fun NonAuthoritativeInformation(): HttpResponse {
            return HttpResponse(HttpStatus.NON_AUTHORITATIVE_INFORMATION)
        }
        fun NoContent(): HttpResponse {
            return HttpResponse(HttpStatus.NO_CONTENT)
        }
        fun ResetContent(): HttpResponse {
            return HttpResponse(HttpStatus.RESET_CONTENT)
        }
        fun PartialContent(): HttpResponse {
            return HttpResponse(HttpStatus.PARTIAL_CONTENT)
        }
        fun MultiStatus(): HttpResponse {
            return HttpResponse(HttpStatus(207, "Multi-Status"))
        }

        fun MultipleChoices(): HttpResponse {
            return HttpResponse(HttpStatus.MULTIPLE_CHOICES)
        }
        fun MovedPermanently(): HttpResponse {
            return HttpResponse(HttpStatus.MOVED_PERMANENTLY)
        }
        fun Found(): HttpResponse {
            return HttpResponse(HttpStatus.FOUND)
        }
        fun SeeOther(): HttpResponse {
            return HttpResponse(HttpStatus.SEE_OTHER)
        }
        fun NotModified(): HttpResponse {
            return HttpResponse(HttpStatus.NOT_MODIFIED)
        }
        fun UseProxy(): HttpResponse {
            return HttpResponse(HttpStatus.USE_PROXY)
        }
        fun TemporaryRedirect(): HttpResponse {
            return HttpResponse(HttpStatus.TEMPORARY_REDIRECT)
        }
        fun PermanentRedirect(): HttpResponse {
            return HttpResponse(HttpStatus.PERMANENT_REDIRECT)
        }

        fun BadRequest(): HttpResponse {
            return HttpResponse(HttpStatus.BAD_REQUEST)
        }
        fun Unauthorized(): HttpResponse {
            return HttpResponse(HttpStatus.UNAUTHORIZED)
        }
        fun PaymentRequired(): HttpResponse {
            return HttpResponse(HttpStatus.PAYMENT_REQUIRED)
        }
        fun Forbidden(): HttpResponse {
            return HttpResponse(HttpStatus.FORBIDDEN)
        }
        fun NotFound(): HttpResponse {
            return HttpResponse(HttpStatus.NOT_FOUND)
        }
        fun MethodNotAllowed(): HttpResponse {
            return HttpResponse(HttpStatus.METHOD_NOT_ALLOWED)
        }
        fun NotAcceptable(): HttpResponse {
            return HttpResponse(HttpStatus.NOT_ACCEPTABLE)
        }
        fun ProxyAuthenticationRequired(): HttpResponse {
            return HttpResponse(HttpStatus.PROXY_AUTHENTICATION_REQUIRED)
        }
        fun RequestTimeout(): HttpResponse {
            return HttpResponse(HttpStatus.REQUEST_TIMEOUT)
        }
        fun Conflict(): HttpResponse {
            return HttpResponse(HttpStatus.CONFLICT)
        }
        fun Gone(): HttpResponse {
            return HttpResponse(HttpStatus.GONE)
        }
        fun LengthRequired(): HttpResponse {
            return HttpResponse(HttpStatus.LENGTH_REQUIRED)
        }
        fun PreconditionFailed(): HttpResponse {
            return HttpResponse(HttpStatus.PRECONDITION_FAILED)
        }
        fun UnsupportedMediaType(): HttpResponse {
            return HttpResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        }
        fun ExpectationFailed(): HttpResponse {
            return HttpResponse(HttpStatus.EXPECTATION_FAILED)
        }
        fun UnprocessableEntity(): HttpResponse {
            return HttpResponse(HttpStatus.UNPROCESSABLE_ENTITY)
        }
        fun UpgradeRequired(): HttpResponse {
            return HttpResponse(HttpStatus.UPGRADE_REQUIRED)
        }

        fun TooManyRequests(): HttpResponse {
            return HttpResponse(HttpStatus.TOO_MANY_REQUESTS)
        }
        fun UnavailableForLegalReasons(): HttpResponse {
            return HttpResponse(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS)
        }

        fun InternalServerError(): HttpResponse {
            return HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR)
        }
        fun NotImplemented(): HttpResponse {
            return HttpResponse(HttpStatus.NOT_IMPLEMENTED)
        }
        fun BadGateway(): HttpResponse {
            return HttpResponse(HttpStatus.BAD_GATEWAY)
        }
        fun ServiceUnavailable(): HttpResponse {
            return HttpResponse(HttpStatus.SERVICE_UNAVAILABLE)
        }
        fun GatewayTimeout(): HttpResponse {
            return HttpResponse(HttpStatus.GATEWAY_TIMEOUT)
        }
        fun VersionNotSupported(): HttpResponse {
            return HttpResponse(HttpStatus.HTTP_VERSION_NOT_SUPPORTED)
        }

    }
}


/**
 * Sets the body of the response to a JSON representation of the data object,
 * using the provided Gson instance for serialization.
 * Returns a new Response object with the appropriate headers set.
 * Handles JsonSyntaxException by returning an INTERNAL_SERVER_ERROR response.
 */
fun <T> HttpResponse.jsonResponse(data: T, gson: Gson = Gson()): HttpResponse {
    return try {
        addHeader("Content-Type", "application/json")
         body(gson.toJson(data))
        return  this
    } catch (e: JsonSyntaxException) {
        HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR)
}
}
/*
fun JsonResponse(data: Any,status : HttpStatus): HttpResponse {

}*/