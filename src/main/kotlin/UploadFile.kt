package org.aquiles

import org.http4k.core.ContentType
import java.io.File
import java.io.InputStream


data class UploadFile(val fileName: String, val contentType: ContentType, val content: InputStream) {
    fun write( targetPath: String) {
        val bytes = content.readBytes()
        val targetFile = File(targetPath, fileName)
        targetFile.parentFile.mkdirs()  // Create directories if they don't exist
        targetFile.createNewFile()
        targetFile.writeBytes(bytes)
    }
}
