package no.nav.sykmelding.proxy

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.exporter.common.TextFormat.write004

fun Routing.registerNaisApi(collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry) {
    get("/is_alive"){
        call.respondText("Alive!")
    }
    get("/is_ready") {
        call.respondText("Ready!")
    }
    get("/prometheus") {
        val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: setOf()
        call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
            write004(this, collectorRegistry.filteredMetricFamilySamples(names))
        }
    }
}
