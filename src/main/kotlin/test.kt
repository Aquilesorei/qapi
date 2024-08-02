/*
import org.aquiles.createDummyInstance
import kotlin.reflect.*
import kotlin.reflect.full.createType
import kotlin.reflect.full.primaryConstructor



// Example data class
data class Student(
    val classroom_name: String,
    val level: String,
    val firstname: String,
    val lastname: String,
    val gender: String,
    val dob: String,
    val cob: String,
    val parent_name: String,
    val parent_phones: List<String>,
    val passe: Boolean,
    val scholarship: Boolean,
    val matricule: String
)

fun main() {
    val kType = Student::class.createType()
    */
/*val dummyInstance = createDummyInstance(kType)
    println(dummyInstance)*//*

}
*/



import io.undertow.Undertow
import io.undertow.server.handlers.PathHandler
import io.undertow.server.handlers.resource.PathResourceManager
import io.undertow.server.handlers.resource.ResourceHandler
import io.undertow.UndertowOptions.ENABLE_HTTP2
import java.nio.file.Paths

fun main() {
    val port = 8080 // Default port
    val host = "0.0.0.0" // Listen on all interfaces
    val downloadDir = System.getProperty("user.home") + "/Downloads" // Customize download directory

    val routingHandler = PathHandler()

    // Download route
    routingHandler.addPrefixPath("/download", ResourceHandler(
        PathResourceManager(Paths.get(downloadDir), 100)
    ).setDirectoryListingEnabled(false)) // Disable directory listing

    // Default resource handler for other content
    routingHandler.addPrefixPath("/", ResourceHandler(
        PathResourceManager(Paths.get(System.getProperty("user.home")), 100)
    ).setDirectoryListingEnabled(false))

    val server = Undertow.builder()
        .addHttpListener(port, host, routingHandler)
        .setServerOption(ENABLE_HTTP2, false)
        .setWorkerThreads(32 * Runtime.getRuntime().availableProcessors())
        .build()

    server.start()
}
