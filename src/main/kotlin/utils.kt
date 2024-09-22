package org.aquiles

import com.andreapivetta.kolor.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import openapi.Property
import org.aquiles.core.ContentType
import org.aquiles.core.HttpRequest
import org.aquiles.core.HttpResponse
import org.aquiles.core.jsonResponse
import org.aquiles.serialization.fromJson

import kotlin.reflect.*
import kotlin.reflect.full.primaryConstructor



internal fun normalizePath(path: String): String {
    return path.replace(Regex("/{2,}"), "/")
}

@OptIn(ExperimentalStdlibApi::class)
internal fun castBasedOnType(value: Any, kType: KType): Any? {
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
internal fun formatRoutePrefix(prefix: String?): String {
    return prefix?.let {
        if (it.isEmpty()) {
            "" // Handle empty prefix case explicitly
        } else {
            if (it.startsWith("/")) it else "/$it"
        }
    } ?: "" // Return empty string for null prefix
}

/**
 * Processes parameters from a request and maps them to function parameters
 */
internal fun processParams(parameters: List<KParameter>, req: HttpRequest, multipartFields: Array<String> = arrayOf(), multipartFiles: Array<String> = arrayOf()): MutableMap<KParameter, Any> {
    val map = mutableMapOf<KParameter, Any>()



   /* if (req.queryParameters.isEmpty() && req.pathParameters.isEmpty()){
        return map
    }*/


    val contentType = req.getHeader("Content-Type")
    parameters.forEach { param ->
        param.name?.let { name ->
            val res = req.query(name) ?: req.path(name)
            if (res != null) {

                if(param.type.classifier == HttpRequest::class){
                    map[param] = req
                }else {
                    castBasedOnType(res, param.type)?.let {
                        map[param] = it
                    }
                }


            } else if (contentType == "application/json") {




                val gson = Gson()
                val mapType = object : TypeToken<Map<String, Any>>() {}.type
                val ma = gson.fromJson<Map<String, Any>>(req.bodyString(), mapType)
                fromJson(ma ,param.type)?.let {
                    map[param] = it
                }

            } else if(contentType?.startsWith("multipart/form-data") == true){

                val files = handleMultipartForm(req, files = multipartFiles, fields = multipartFields)
                when {
                    isListUploadFile(param.type) -> map[param] = files
                    param.type.classifier == UploadFile::class && files.isNotEmpty() -> map[param] = files[0]
                }
            }else    if(param.type.classifier == HttpRequest::class){
                map[param] = req
            }
        }
    }

    return map
}


/**
 * Handles multipart form data from a request
 */
private fun handleMultipartForm(request: HttpRequest, fields: Array<String>, files: Array<String>): List<UploadFile> {
    val list = mutableListOf<UploadFile>()
/*    val multipartForm = MultipartFormBody.from(request)




    files.forEach { field ->
        multipartForm.file(field)?.let { fileInput ->
            val up = UploadFile(fileInput.filename, fileInput.contentType, fileInput.content)
            list.add(up)
        }
    }*/

    return list
}


internal fun logRequest(request: HttpRequest, response : HttpResponse) {

    print("INFO:  ".green())
    print(" \"${request.method.name} ${request.uri} ${request.version}\" ")
    if(response.statusCode.code >=400){
        println("${response.statusCode.code} ${response.statusCode.description}".red())
    }else{
        println("${response.statusCode.code} ${response.statusCode.description}".green())
    }


}


internal fun convertToSentenceCase(input: String): String {
    // Normalize separators and handle edge cases
    val normalized = input.trim().replace("_", "-")

    // Split into words based on case changes or separators
    val words = normalized.split(Regex("(?<=[a-z])(?=[A-Z])|-")).filter { it.isNotEmpty() }

    // Capitalize the first letter of each word and make the rest lowercase
    val capitalizedWords = words.map { it.toLowerCase().capitalize() }

    // Join words back into a sentence with spaces
    return capitalizedWords.joinToString(" ")
}


internal fun List<Pair<String, String>>.toMutableMap(): MutableMap<String, String> {
    return this.associateTo(mutableMapOf()) { it }
}

/*
fun  getContenType(function: KCallable<*>): ContentType? {
    val type = function.returnType;
    return when(type.classifier) {
        null -> null
         Response::class -> null
        String::class  -> ContentType.TEXT_PLAIN
         Number::class, Boolean::class,  Enum::class, Pair::class , Collection::class,Map::class-> ContentType.APPLICATION_JSON
        else -> handleCustomResponse(res)
    }
    }
}*/
