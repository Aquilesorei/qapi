package core

import org.http4k.asByteBuffer
import org.http4k.asString
import org.http4k.length
import java.io.Closeable
import java.io.InputStream
import java.nio.ByteBuffer

/**
 * If this Body is NOT being returned to the caller (via a Server implementation or otherwise), close() should be
 * called.
 */
interface Body : Closeable {
    val stream: InputStream
    val payload: ByteBuffer

    /**
     * Will be `null` for bodies where it's impossible to a priori determine - e.g. StreamBody
     */
    val length: Long?

    companion object {
        @JvmStatic
        @JvmName("create")
        operator fun invoke(body: String): Body = MemoryBody(body)

        @JvmStatic
        @JvmName("create")
        operator fun invoke(body: ByteBuffer): Body = when {
            body.hasArray() -> MemoryBody(body)
            else -> MemoryBody(ByteArray(body.remaining()).also { body.get(it) })
        }

        @JvmStatic
        @JvmName("create")
        operator fun invoke(body: InputStream, length: Long? = null): Body = StreamBody(body, length)

        @JvmField
        val EMPTY: Body = MemoryBody(ByteBuffer.allocate(0))
    }
}

/**
 * Represents a body that is backed by an in-memory ByteBuffer. Closing this has no effect.
 **/
data class MemoryBody(override val payload: ByteBuffer) : Body {
    constructor(payload: String) : this(payload.asByteBuffer())
    constructor(payload: ByteArray) : this(ByteBuffer.wrap(payload))

    override val length get() = payload.length().toLong()
    override fun close() {}
    override val stream get() = payload.array().inputStream(payload.position(), payload.length())
    override fun toString() = payload.asString()
}

/**
 * Represents a body that is backed by a (lazy) InputStream. Operating with StreamBody has a number of potential
 * gotchas:
 * 1. Attempts to consume the stream will pull all of the contents into memory, and should thus be avoided.
 * This includes calling `equals()` and `payload`
 * 2. If this Body is NOT being returned to the caller (via a Server implementation or otherwise), close() should be called.
 * 3. Depending on the source of the stream, this body may or may not contain a known length.
 */
class StreamBody(override val stream: InputStream, override val length: Long? = null) : Body {
    override val payload: ByteBuffer by lazy { stream.use { ByteBuffer.wrap(it.readBytes()) } }

    override fun close() {
        stream.close()
    }

    override fun toString() = "<<stream>>"

    override fun equals(other: Any?) =
        when {
            this === other -> true
            other !is Body? -> false
            else -> payload == other?.payload
        }

    override fun hashCode() = payload.hashCode()
}

data class  Part(val headers: Map<String, String>, val body: Body)




interface HTTPMessage  : Closeable{
    val headers: MutableMap<String, HeaderValue>
    var body: Body


    /**
     *
     * adds a header to headers
     */
    fun addHeader(name: String, value: String)
    fun getHeader(name: String): String?
    fun getHeaderDirectives(name: String): Map<String, String?>?

    fun removeHeader(name: String)
    fun bodyString(): String = String(body.payload.array())

    override fun  close() = body.close()
}








data class HeaderValue(
    val firstDirective: String,
    val parameters: MutableMap<String, String?> = mutableMapOf()
) {
    override fun toString(): String {
        val parametersString = parameters.entries.joinToString("; ") { "${it.key}=${it.value}" }

        return buildString {
            append(firstDirective)
            if (parametersString.isNotBlank()) append("; ").append(parametersString)
        }
    }
}





fun io. undertow. util. HeaderMap.toHeaderMap(): MutableMap<String, HeaderValue> {
    val headerMap = mutableMapOf<String, HeaderValue>()

    for (header in this) {
        val name = header.headerName.toString()
        val value = header.first()
        val parameters = parseParameters(value)

        headerMap[name] = HeaderValue(
            firstDirective = value.substringBefore(";").trim(),
            parameters = parameters.toMutableMap()
        )
    }

    return headerMap
}

private fun parseParameters(headerValue: String): Map<String, String> {
    return headerValue.split(";")
        .drop(1) // Skip the media type part
        .mapNotNull { param ->
            val equalsIndex = param.indexOf('=')
            if (equalsIndex == -1) return@mapNotNull null // Skip invalid parameters

            val key = param.substring(0, equalsIndex).trim()
            val value = param.substring(equalsIndex + 1).trim()
            key to value // Create a Pair and return it
        }
        .toMap()
}

