package core

import org.aquiles.core.HttpHandler
import org.aquiles.core.HttpRequest
import java.util.regex.Pattern

data class RouteData(val method: String, var path : String, var handler: HttpHandler){

    var pathPattern: Pattern
    init {
        pathPattern = Pattern.compile(pathToRegex(path))
    }



    private fun pathToRegex(path: String): String {
        return path.replace("{", "(?<").replace("}", ">[^/]+)") + "$"
    }
    fun  matches(request: HttpRequest): Boolean {
        return  method.equals(method, ignoreCase = true) && pathPattern.matcher(request.path).matches()

    }
}