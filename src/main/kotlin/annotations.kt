package org.aquiles

import org.aquiles.core.HttpMethod


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EndPoint(val method: HttpMethod, val path: String,val contentType: String = "application/json")

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Get(val path: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Post(val path: String,val multipartFields: Array<String> = [],val multipartFiles: Array<String> = ["file"])


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Put(val path: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Delete(val path: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Patch(val path: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Head(val path: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Options(val path: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Trace(val path: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Purge(val path: String)


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class OpenAPIProperty(
    val type: String,
    val format: String = "",   // Default to an empty string
    val description: String = "", // Default to an empty string
    val required: Boolean = false,
    val nullable: Boolean = true,
    val default: String = "",  // Default to an empty string
    val example: String = "", // Default to an empty string
    val readOnly: Boolean = false,
    val writeOnly: Boolean = false,

    // String constraints
    val minLength: Int = -1, // Use -1 to indicate not set
    val maxLength: Int = -1, // Use -1 to indicate not set
    val pattern: String = "",

    // Numeric constraints
    val minimum: Double = Double.NEGATIVE_INFINITY,  // Use infinity for no minimum
    val maximum: Double = Double.POSITIVE_INFINITY,  // Use infinity for no maximum
    val exclusiveMinimum: Boolean = false,
    val exclusiveMaximum: Boolean = false,
    val multipleOf: Double = -1.0, // Use -1.0 to indicate not set

    // Array constraints
    val minItems: Int = -1, // Use -1 to indicate not set
    val maxItems: Int = -1, // Use -1 to indicate not set
    val uniqueItems: Boolean = false,

    // Additional properties (for objects)
    val additionalProperties: Boolean = true,  // Default to allow additional properties

    // Deprecated flag
    val deprecated: Boolean = false
)
