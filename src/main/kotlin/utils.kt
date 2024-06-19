package org.aquiles

import com.andreapivetta.kolor.*
import com.google.gson.Gson
import org.http4k.core.Request
import org.http4k.core.Response
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.javaType

@OptIn(ExperimentalStdlibApi::class)
fun castBasedOnType(value: Any, kType: KType): Any? {
    val type = kType.classifier
    return when (type) {
        is KClass<*> -> {
            when (type) {
                String::class -> value as? String
                Int::class -> (value as? String)?.toIntOrNull()
                Double::class -> (value as? String)?.toDoubleOrNull()
                Boolean::class -> (value as? String)?.toBoolean()
                Long::class -> (value as? String)?.toLongOrNull()
                Float::class -> (value as? String)?.toFloatOrNull()
                Short::class -> (value as? String)?.toShortOrNull()
                Byte::class -> (value as? String)?.toByteOrNull()
                Char::class -> {
                    val stringValue = value as? String
                    if (stringValue?.length == 1) stringValue[0] else null
                }
               // Unit::class -> Unit // Return Unit if the type is Unit
                else -> {
                    // Handle JSON deserialization if the type is a data class
                    if (value is String) {
                        val gson = Gson()
                        try {
                            gson.fromJson(value, kType.javaType)
                        } catch (e: Exception) {
                            // Handle parsing exception (e.g., log error)
                            null
                        }
                    } else {
                        null
                    }
                }
            }
        }
        else -> {
            // Handle custom logic for other types (optional)
            null
        }
    }
}
fun formatRoutePrefix(prefix: String?): String {
    return prefix?.let {
        if (it.isEmpty()) {
            "" // Handle empty prefix case explicitly
        } else {
            if (it.startsWith("/")) it else "/$it"
        }
    } ?: "" // Return empty string for null prefix
}






fun logRequest(request: Request, response : Response) {

    print("INFO:  ".green())
    print(request.uri.userInfo)
    print(" \"${request.method.name} ${request.uri.path} ${request.version}\" ")
    if(response.status.code >=400){
        println("${response.status.code} ${response.status.description}".red())
    }else{
        println("${response.status.code} ${response.status.description}".green())
    }


}