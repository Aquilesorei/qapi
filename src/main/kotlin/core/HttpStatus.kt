package org.aquiles.core



class HttpStatus internal constructor(val code: Int, val description: String, private val clientGenerated: Boolean = false) {

    constructor(code: Int, description: String?) : this(code, description ?: "No description", false)

    companion object {
        private val INFORMATIONAL = 100..199
        @JvmField val CONTINUE = HttpStatus(100, "Continue")
        @JvmField val SWITCHING_PROTOCOLS = HttpStatus(101, "Switching Protocols")

        private val SUCCESSFUL = 200..299
        @JvmField val OK = HttpStatus(200, "OK")
        @JvmField val CREATED = HttpStatus(201, "Created")
        @JvmField val ACCEPTED = HttpStatus(202, "Accepted")
        @JvmField val NON_AUTHORITATIVE_INFORMATION = HttpStatus(203, "Non-Authoritative Information")
        @JvmField val NO_CONTENT = HttpStatus(204, "No Content")
        @JvmField val RESET_CONTENT = HttpStatus(205, "Reset Content")
        @JvmField val PARTIAL_CONTENT = HttpStatus(206, "Partial Content")

        private val REDIRECTION = 300..399
        @JvmField val MULTIPLE_CHOICES = HttpStatus(300, "Multiple Choices")
        @JvmField val MOVED_PERMANENTLY = HttpStatus(301, "Moved Permanently")
        @JvmField val FOUND = HttpStatus(302, "Found")
        @JvmField val SEE_OTHER = HttpStatus(303, "See Other")
        @JvmField val NOT_MODIFIED = HttpStatus(304, "Not Modified")
        @JvmField val USE_PROXY = HttpStatus(305, "Use Proxy")
        @JvmField val TEMPORARY_REDIRECT = HttpStatus(307, "Temporary Redirect")
        @JvmField val PERMANENT_REDIRECT = HttpStatus(308, "Permanent Redirect")

        private val CLIENT_ERROR = 400..499
        @JvmField val BAD_REQUEST = HttpStatus(400, "Bad Request")
        @JvmField val UNSATISFIABLE_PARAMETERS = BAD_REQUEST.description("Unsatisfiable Parameters")
        @JvmField val UNAUTHORIZED = HttpStatus(401, "Unauthorized")
        @JvmField val PAYMENT_REQUIRED = HttpStatus(402, "Payment Required")
        @JvmField val FORBIDDEN = HttpStatus(403, "Forbidden")
        @JvmField val NOT_FOUND = HttpStatus(404, "Not Found")
        @JvmField val METHOD_NOT_ALLOWED = HttpStatus(405, "Method Not Allowed")
        @JvmField val NOT_ACCEPTABLE = HttpStatus(406, "Not Acceptable")
        @JvmField val PROXY_AUTHENTICATION_REQUIRED = HttpStatus(407, "Proxy Authentication Required")
        @JvmField val REQUEST_TIMEOUT = HttpStatus(408, "Request Timeout")
        @JvmField val CONFLICT = HttpStatus(409, "Conflict")
        @JvmField val GONE = HttpStatus(410, "Gone")
        @JvmField val LENGTH_REQUIRED = HttpStatus(411, "Length Required")
        @JvmField val PRECONDITION_FAILED = HttpStatus(412, "Precondition Failed")
        @JvmField val REQUEST_ENTITY_TOO_LARGE = HttpStatus(413, "Request Entity Too Large")
        @JvmField val REQUEST_URI_TOO_LONG = HttpStatus(414, "Request-URI Too Long")
        @JvmField val UNSUPPORTED_MEDIA_TYPE = HttpStatus(415, "Unsupported Media Type")
        @JvmField val REQUESTED_RANGE_NOT_SATISFIABLE = HttpStatus(416, "Requested Range Not Satisfiable")
        @JvmField val EXPECTATION_FAILED = HttpStatus(417, "Expectation Failed")
        @JvmField val I_M_A_TEAPOT = HttpStatus(418, "I'm a teapot") //RFC2324
        @JvmField val UNPROCESSABLE_ENTITY = HttpStatus(422, "Unprocessable Entity")
        @JvmField val UPGRADE_REQUIRED = HttpStatus(426, "Upgrade Required")
        @JvmField val TOO_MANY_REQUESTS = HttpStatus(429, "Too many requests")
        @JvmField val UNAVAILABLE_FOR_LEGAL_REASONS = HttpStatus(451, "Unavailable For Legal Reasons")

        private val SERVER_ERROR = 500..599
        @JvmField val INTERNAL_SERVER_ERROR = HttpStatus(500, "Internal Server Error")
        @JvmField val NOT_IMPLEMENTED = HttpStatus(501, "Not Implemented")
        @JvmField val BAD_GATEWAY = HttpStatus(502, "Bad Gateway")
        @JvmField val SERVICE_UNAVAILABLE = HttpStatus(503, "Service Unavailable")
        @JvmField val CONNECTION_REFUSED = HttpStatus(503, "Connection Refused", true)
        @JvmField val UNKNOWN_HOST = HttpStatus(503, "Unknown Host", true)
        @JvmField val GATEWAY_TIMEOUT = HttpStatus(504, "Gateway Timeout")
        @JvmField val CLIENT_TIMEOUT = HttpStatus(504, "Client Timeout", true)
        @JvmField val HTTP_VERSION_NOT_SUPPORTED = HttpStatus(505, "HTTP Version Not Supported")

        val serverValues by lazy {
            listOf(
                CONTINUE,
                SWITCHING_PROTOCOLS,
                OK,
                CREATED,
                ACCEPTED,
                NON_AUTHORITATIVE_INFORMATION,
                NO_CONTENT,
                RESET_CONTENT,
                PARTIAL_CONTENT,
                MULTIPLE_CHOICES,
                MOVED_PERMANENTLY,
                FOUND,
                SEE_OTHER,
                NOT_MODIFIED,
                USE_PROXY,
                TEMPORARY_REDIRECT,
                PERMANENT_REDIRECT,
                BAD_REQUEST,
                UNSATISFIABLE_PARAMETERS,
                UNAUTHORIZED,
                PAYMENT_REQUIRED,
                FORBIDDEN,
                NOT_FOUND,
                METHOD_NOT_ALLOWED,
                NOT_ACCEPTABLE,
                PROXY_AUTHENTICATION_REQUIRED,
                REQUEST_TIMEOUT,
                CONFLICT,
                GONE,
                LENGTH_REQUIRED,
                PRECONDITION_FAILED,
                REQUEST_ENTITY_TOO_LARGE,
                REQUEST_URI_TOO_LONG,
                UNSUPPORTED_MEDIA_TYPE,
                REQUESTED_RANGE_NOT_SATISFIABLE,
                EXPECTATION_FAILED,
                I_M_A_TEAPOT,
                UNPROCESSABLE_ENTITY,
                UPGRADE_REQUIRED,
                TOO_MANY_REQUESTS,
                UNAVAILABLE_FOR_LEGAL_REASONS,
                INTERNAL_SERVER_ERROR,
                NOT_IMPLEMENTED,
                BAD_GATEWAY,
                SERVICE_UNAVAILABLE,
                CONNECTION_REFUSED,
                UNKNOWN_HOST,
                GATEWAY_TIMEOUT,
                CLIENT_TIMEOUT,
                HTTP_VERSION_NOT_SUPPORTED
            ).filterNot { it.clientGenerated }
        }

        fun fromCode(code: Int) = serverValues.firstOrNull { it.code == code }
    }

    val successful by lazy { SUCCESSFUL.contains(code) }
    val informational by lazy { INFORMATIONAL.contains(code) }
    val redirection by lazy { REDIRECTION.contains(code) }
    val clientError by lazy { CLIENT_ERROR.contains(code) || clientGenerated }
    val serverError by lazy { SERVER_ERROR.contains(code) }

    fun description(newDescription: String) = HttpStatus(code, newDescription, clientGenerated)

    override fun hashCode(): Int = code.hashCode() + clientGenerated.hashCode()

    override fun toString(): String = "$code $description"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpStatus

        if (code != other.code) return false
        if (clientGenerated != other.clientGenerated) return false

        return true
    }
}






