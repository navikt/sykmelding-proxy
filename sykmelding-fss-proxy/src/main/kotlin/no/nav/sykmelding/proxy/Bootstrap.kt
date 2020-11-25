package no.nav.sykmelding.proxy

import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.features.CallId
import io.ktor.http.HttpHeaders
import io.ktor.request.ApplicationRequest
import io.ktor.request.header
import io.ktor.request.path
import io.ktor.routing.Routing
import io.ktor.server.netty.EngineMain
import io.prometheus.client.hotspot.DefaultExports

private val monitoringPaths = listOf("/is_alive", "/is_ready", "/prometheus")
private const val navCallIdHeader = "Nav-Call-Id"

fun main(args: Array<String>) : Unit = EngineMain.main(args)

fun Application.sykmeldingProxy() {
    val client = HttpClient(Apache)
    val proxyHandler = ProxyHandler(Environment().proxyMappings, client)
    setUpApp(proxyHandler)
}

fun Application.setUpApp(proxyHandler: ProxyHandler) {
    DefaultExports.initialize()
    install(Routing) {
        registerNaisApi()
    }
    install(CallId) {
        retrieve { call ->
            call.request.header(HttpHeaders.XCorrelationId) ?: call.request.header(navCallIdHeader)
        }
    }

    intercept(ApplicationCallPipeline.Call) {
        when (call.request.isMonitoring()) {
            false -> proxyHandler.handleProxyEvent(call)
        }
    }
}

private fun ApplicationRequest.isMonitoring(): Boolean {
    return monitoringPaths.contains(path())
}
