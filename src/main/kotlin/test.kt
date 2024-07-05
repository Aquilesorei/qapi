/*


import com.google.gson.Gson
import io.undertow.server.HttpHandler
import io.undertow.server.HttpServerExchange
import io.undertow.util.Headers
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import java.time.YearMonth

@Path("/api/demo")
class DemoHandler : HttpHandler {

    override fun handleRequest(exchange: HttpServerExchange) {
        exchange.responseHeaders.put(Headers.CONTENT_TYPE, "application/json")
        val params = exchange.queryParameters
        val month = params["month"]?.first?.toInt() ?: 0
        val year = params["year"]?.first?.toInt() ?: 0
        exchange.responseSender.send(Gson().toJson(getDaysIn(year, month)))
    }

    @GET
    @Operation(
        summary = "Get number of days in the given month and year",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "A number of days in the given month and year",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = Int::class))]
            )
        ]
    )
    fun getDaysIn( year: Int,month: Int): Int {
        return YearMonth.of(year, month).lengthOfMonth()
    }
}
*/
