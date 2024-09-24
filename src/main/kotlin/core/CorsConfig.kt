package core


/**
 * Configuration class for Cross-Origin Resource Sharing (CORS) settings.
 *
 * @property allowedOrigins A list of allowed origins (domains) that can make cross-origin requests.
 *                          Defaults to "*" (all origins).
 * @property allowedMethods A list of allowed HTTP methods (e.g., GET, POST, PUT) for cross-origin requests.
 * @property allowedHeaders A list of allowed headers that can be sent in cross-origin requests.
 * @property allowCredentials Whether to allow credentials (cookies, authorization headers) in cross-origin requests.
 *                          Defaults to true.
 * @property maxAge The maximum time (in seconds) that browsers can cache the results of a preflight request.
 *                          Defaults to 3600 seconds (1 hour).
 */
data class CorsConfig(
    val allowedOrigins: List<String> = listOf("*"),
    val allowedMethods: List<String> = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS"),
    val allowedHeaders: List<String> = listOf("Content-Type", "Authorization"),
    val allowCredentials: Boolean = true,
    val maxAge: Long = 3600L
)