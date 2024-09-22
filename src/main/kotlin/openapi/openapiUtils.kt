package openapi

import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.KClass
import kotlin.reflect.KType



fun getOpenApiPrimitiveType(kType: KType): String {
    val classifier = kType.classifier

    return if (classifier is KClass<*>) {
        when (classifier) {
            String::class -> "string"
            Int::class, Long::class -> "integer"
            Float::class, Double::class -> "number"
            Boolean::class -> "boolean"
            else -> "object" // Not an OpenAPI primitive type
        }
    } else {
        "undefined"
    }
}



fun getOpenApiFormat(kType: KType): String? {
    val classifier = kType.classifier

    if (classifier is KClass<*>) {
        return when (classifier) {
            String::class -> {
                // For strings, you might want to check for specific formats
                // (e.g., "email", "date", "date-time") based on annotations or other metadata
                // For now, let's just return "string"
                "string"
            }
            Int::class -> {
              "integer32"
            }
            Long::class -> {
                "integer64"
            }
            Float::class -> {
                "float"
            }
            Double::class -> {
                "double"
            }
            Boolean::class -> "boolean"
            else -> null // Not a supported OpenAPI primitive type
        }
    } else {
        null // Not a KClass, so not a primitive type
    }
    return null
}


fun KType.isPrimitive(): Boolean {
    return this.classifier == String::class || this.classifier == Int::class || this.classifier == Long::class || this.classifier == Float::class || this.classifier == Boolean::class || this.classifier == Double::class
}



fun KType.isDataClass(): Boolean {
    val kClass = this.classifier as? KClass<*> // Get the KClass from the KType
    return kClass?.isData ?: false // Check if the KClass represents a data class
}
fun createDummyInstance(kType: KType): Any? {
    val classifier = kType.classifier as? KClass<*> ?: return null

    // Handling basic types
    return when (classifier) {
        String::class -> "string"
        Int::class -> 0
        Long::class -> 0L
        Float::class -> 0.0f
        Double::class -> 0.0
        Boolean::class -> true
        List::class -> listOf("string")
        else -> {
            try {
                // Get primary constructor
                val primaryConstructor = classifier.primaryConstructor ?: return null

                // Create dummy values for constructor parameters
                val args = primaryConstructor.parameters.associateWith { parameter ->
                    createDummyInstance(parameter.type)
                }

                // Create instance with dummy values
                primaryConstructor.callBy(args)
            } catch (e: Exception) {
               // e.printStackTrace()
                null
            }
        }
    }
}