package org.aquiles

import org.http4k.contract.ContractRoute
import org.http4k.contract.contract
import org.http4k.contract.meta
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.lens.string

import org.http4k.contract.ui.redocLite
import org.http4k.contract.ui.swaggerUiLite
import org.http4k.core.*
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer


fun createContractHandler(summary : String,path : String,method: Method,handler : HttpHandler,): ContractRoute {
    val greetingLens = Body.string(ContentType.TEXT_PLAIN).toLens()


    // Define a single http route for our contract
    val contractRoute = path meta {
        operationId = path.replace("/","")
        this.summary = summary
        returning(Status.OK, greetingLens to "Sample Greeting")
    } bindContract method to handler

    return  contractRoute
}




fun  createAll( employementContracts : List<ContractRoute>) =  contract {
    routes += employementContracts
    renderer = OpenApi3(
        ApiInfo("QuickAPI", "0.1.0")
    )
    this.descriptionPath = "/openapi.json"
}

