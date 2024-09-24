package org.aquiles


import org.aquiles.core.ContentType
import java.io.File
import java.io.InputStream
import kotlin.reflect.KType

/**
 * Represents an uploaded file with its metadata and content.
 *
 * @param fileName The original name of the uploaded file.
 * @param contentType The MIME type of the file (e.g., "image/jpeg", "application/pdf").
 * @param content An InputStream providing access to the file's binary data.
 */
data class UploadFile(val fileName: String, val contentType: ContentType, val content: InputStream) {

    /**
     * Writes the file content to the specified target path.
     *
     * @param targetPath The directory where the file should be saved.
     */
    fun write(targetPath: String) {
        val targetFile = File(targetPath, fileName)
        targetFile.parentFile.mkdirs()  // Create parent directories if they don't exist
        targetFile.createNewFile()      // Create the file if it doesn't exist

        // Use 'use' blocks for automatic resource management (closing streams)
        content.use { input ->
            targetFile.outputStream().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytesRead: Int
                // Read and write data in chunks to avoid loading the entire file into memory
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 8192 // Default buffer size for reading/writing
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
