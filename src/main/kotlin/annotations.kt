package org.aquiles

import org.http4k.core.Method

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EndPoint(val method: Method, val path: String,)
