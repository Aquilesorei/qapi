package openapi

sealed class OpenApiPrimitive {

    abstract val type: String

    data class OpenApiString(
        override val type: String = "string",
        val minLength: Int? = null,
        val maxLength: Int? = null,
        val pattern: String? = null,
        val format: String? = null
    ) : OpenApiPrimitive()

    data class OpenApiInteger(
        override val type: String = "integer",
        val format: String? = null,
        val minimum: Int? = null,
        val maximum: Int? = null,
        val exclusiveMinimum: Boolean? = null,
        val exclusiveMaximum: Boolean? = null,
        val multipleOf: Number? = null
    ) : OpenApiPrimitive()

    data class OpenApiNumber(
        override val type: String = "number",
        val format: String? = null,
        val minimum: Number? = null,
        val maximum: Number? = null,
        val exclusiveMinimum: Boolean? = null,
        val exclusiveMaximum: Boolean? = null,
        val multipleOf: Number? = null
    ) : OpenApiPrimitive()

    data class OpenApiBoolean(
        override val type: String = "boolean"
    ) : OpenApiPrimitive()

    data class OpenApiArray(
        override val type: String = "array",
        val items: OpenApiPrimitive, // Change to OpenApiPrimitive
        val minItems: Int? = null,
        val maxItems: Int? = null,
        val uniqueItems: Boolean? = null
    ) : OpenApiPrimitive()
}


object OpenApiPrimitiveFactory {

    fun create(type: String, constraints: Map<String, Any>? = null): OpenApiPrimitive {
        return when (type) {
            "string" -> {
                OpenApiPrimitive.OpenApiString(
                    minLength = constraints?.get("minLength") as? Int,
                    maxLength = constraints?.get("maxLength") as? Int,
                    pattern = constraints?.get("pattern") as? String,
                    format = constraints?.get("format") as? String
                )
            }
            "integer" -> {
                OpenApiPrimitive.OpenApiInteger(
                    format = constraints?.get("format") as? String,
                    minimum = constraints?.get("minimum") as? Int,
                    maximum = constraints?.get("maximum") as? Int,
                    exclusiveMinimum = constraints?.get("exclusiveMinimum") as? Boolean,
                    exclusiveMaximum = constraints?.get("exclusiveMaximum") as? Boolean,
                    multipleOf = constraints?.get("multipleOf") as? Number
                )
            }
            "number" -> {
                OpenApiPrimitive.OpenApiNumber(
                    format = constraints?.get("format") as? String,
                    minimum = constraints?.get("minimum") as? Number,
                    maximum = constraints?.get("maximum") as? Number,
                    exclusiveMinimum = constraints?.get("exclusiveMinimum") as? Boolean,
                    exclusiveMaximum = constraints?.get("exclusiveMaximum") as? Boolean,
                    multipleOf = constraints?.get("multipleOf") as? Number
                )
            }
            "boolean" -> OpenApiPrimitive.OpenApiBoolean()
            "array" -> {
                val itemsType = constraints?.get("items") as? String ?: "string"
                val items = create(itemsType)
                OpenApiPrimitive.OpenApiArray(
                    items = items,
                    minItems = constraints?.get("minItems") as? Int,
                    maxItems = constraints?.get("maxItems") as? Int,
                    uniqueItems = constraints?.get("uniqueItems") as? Boolean
                )
            }

            else -> throw IllegalArgumentException("Unsupported OpenAPI type: $type")
        }
    }
}
