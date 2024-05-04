package org.aquiles

import com.google.gson.Gson
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
