package org.aquiles.core



enum class HttpMethod {
    GET, POST, PUT, DELETE, OPTIONS, TRACE, PATCH, PURGE, HEAD;

    companion object{
        fun fromString(data : String): HttpMethod {
           return when(data){
                "GET" -> GET
                "POST" -> POST
                "PUT" -> PUT
                "DELETE" -> DELETE
                "OPTIONS" -> OPTIONS
                "TRACE" -> TRACE
                "PATCH" -> PATCH
                "PURGE" -> PURGE
                "HEAD" -> HEAD

               else -> {
                   throw IllegalArgumentException("Unknown HTTP method: $data")
               }
           }
        }
    }
}

