package core

import java.io.InputStream





import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers
import io.undertow.server.handlers.form.FormDataParser
import io.undertow.server.handlers.form.FormData
import io.undertow.server.handlers.form.FormParserFactory
import io.undertow.util.BadRequestException
import org.aquiles.core.HttpRequest
import org.http4k.core.Request
import org.http4k.multipart.MultipartFile




/*
private fun processFileItem(name: String, value: FormData.FormValue, request: HttpRequest) {
    val fileName = value.fileName
    if (!fileName.isNullOrEmpty() && !fileName.isNotBlank()) {
        val item = value.fileItem
        val content_type =  value.headers.getFirst(Headers.CONTENT_TYPE)
    }
}

private fun processFormItem(name: String, value: FormData.FormValue, request: HttpRequest) {
    logger.debug("[request:form] {}={}", name, FieldLogParam(name, value.value))
    request.formParams[name] = value.value
}*/



data class MultipartEntity(
    val contentType: String,
    val boundary: String,
    val parts: Sequence<MultipartPart> = emptySequence() // Initialize as empty
) {

    fun parseForm(exchange: HttpServerExchange): MultipartEntity {
        val formData = exchange.getAttachment(FormDataParser.FORM_DATA) ?: return this

        val parts = formData.asSequence().mapNotNull {
            val name = it;
            val  value = formData.getFirst(name)
            if (value.isFileItem) {
                createMultipartFilePart(name, value)
            } else {
                createMultipartTextPart(name, value)
            }
        }

        return this.copy(parts = parts)
    }

    private fun createMultipartFilePart(name: String, value: FormData.FormValue): MultipartFilePart? {
        val fileName = value.fileName
        if (fileName.isBlank()) return null // Skip empty file names

        val item = value.fileItem
        val contentType = value.headers.getFirst(Headers.CONTENT_TYPE)
        val contentDisposition = value.headers.getFirst(Headers.CONTENT_DISPOSITION)

        return MultipartFilePart(
            headers = value.headers.associate { it.headerName.toString() to it.first() },
            contentDisposition = contentDisposition,
            filename = fileName,
            contentType = contentType ?: "application/octet-stream",  // Default if not provided
            inputStream = item.inputStream,
            partName = name
        )
    }

    private fun createMultipartTextPart(name: String, value: FormData.FormValue): MultipartTextPart {
        val contentDisposition = value.headers.getFirst(Headers.CONTENT_DISPOSITION)
        return MultipartTextPart(
            headers = value.headers.associate { it.headerName.toString() to it.first() },
            contentDisposition = contentDisposition,
            content = value.value,
            partName = name
        )
    }
}


sealed interface MultipartPart {
    val headers: Map<String, String>
    val contentDisposition: String?
    val partName : String
}

data class MultipartTextPart(
    override val headers: Map<String, String>,
    override val contentDisposition: String?,
    val content: String,
    override val  partName: String
) : MultipartPart {


}

data class MultipartFilePart(
    override val headers: Map<String, String>,
    override val contentDisposition: String?,
    override val  partName: String,
    val filename: String,
    val contentType: String,
    val inputStream: InputStream
) : MultipartPart
