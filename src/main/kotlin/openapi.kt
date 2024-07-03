package org.aquiles

import org.aquiles.core.*
import org.http4k.contract.ContractRoute
import org.http4k.contract.contract
import org.http4k.contract.meta
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.core.Body
import org.http4k.lens.string



/*

fun createContractHandler(summary : String,path : String,method: Method,handler : HttpHandler,contentType : ContentType): ContractRoute {
    val biDiBodyLens = Body.string(contentType).toLens()


    // Define a single http route for our contract
    val contractRoute = path meta {
        operationId = path.replace("/","")
        this.summary = summary
        returning(HttpStatus.OK, biDiBodyLens to "Sample Greeting")
    } bindContract method to handler

    return  contractRoute
}




fun  createAll(employmentContracts : List<ContractRoute>) =  contract {
    routes += employmentContracts
    renderer = OpenApi3(
        ApiInfo("QuickAPI", "0.1.0")
    )
    this.descriptionPath = "/openapi.json"
}

*/
