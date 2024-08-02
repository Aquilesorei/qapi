package org.aquiles


import org.aquiles.core.ContentType
import java.io.File
import java.io.InputStream
import kotlin.reflect.KType


data class UploadFile(val fileName: String, val contentType: ContentType, val content: InputStream) {
    fun write( targetPath: String) {
        val bytes = content.readBytes()
        val targetFile = File(targetPath, fileName)
        targetFile.parentFile.mkdirs()  // Create directories if they don't exist
        targetFile.createNewFile()
        targetFile.writeBytes(bytes)
    }
}


/**
 * Checks if a given type is a List of UploadFile
 */
 fun isListUploadFile(type: KType): Boolean {
    if (type.classifier != List::class) {
        return false
    }

    val argumentType = type.arguments.firstOrNull()?.type
    return argumentType != null && argumentType.classifier == UploadFile::class
}
