package org.aquiles.core

import java.nio.charset.Charset
import java.util.Locale.getDefault
import kotlin.text.Charsets.UTF_8


data class ContentType(val value: String, val directives: List<Pair<String,String?>> = emptyList()) {

    fun withNoDirectives() = copy(directives = emptyList())

    fun toHeaderValue()  : String = (
            listOf(value) +
                    directives
                        .map { it.first + (it.second?.let { "=$it" } ?: "") }
            ).joinToString("; ")

    fun equalsIgnoringDirectives(that: ContentType): Boolean = withNoDirectives() == that.withNoDirectives()

    companion object {
        private fun text(value: String, charset: Charset? = UTF_8): ContentType {
            val charsetParam : Pair<String, String>?  = charset?.let {
                "charset" to it.name().lowercase(getDefault())
            }
            val params = listOfNotNull(charsetParam)
            return ContentType(value, params)
        }

        fun MultipartFormWithBoundary(boundary: String): ContentType = ContentType("multipart/form-data", listOf("boundary" to boundary))
        fun MultipartMixedWithBoundary(boundary: String): ContentType = ContentType("multipart/mixed", listOf("boundary" to boundary))

        val APPLICATION_FORM_URLENCODED = text("application/x-www-form-urlencoded")
        val APPLICATION_JSON = text("application/json")
        val APPLICATION_ND_JSON = text("application/x-ndjson")
        val APPLICATION_PDF = text("application/pdf")
        val APPLICATION_XML = text("application/xml")
        val APPLICATION_YAML = text("application/yaml")
        val MULTIPART_FORM_DATA = text("multipart/form-data")
        val MULTIPART_MIXED = text("multipart/mixed")
        val OCTET_STREAM = ContentType("application/octet-stream")
        val TEXT_CSV = text("text/csv")
        val TEXT_EVENT_STREAM = text("text/event-stream")
        val TEXT_PLAIN = text("text/plain")
        val TEXT_HTML = text("text/html")
        val TEXT_XML = text("text/xml")
        val TEXT_YAML = text("text/yaml")
    }
}
