package no.nav.sykmelding.proxy

import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun runTestServer(proxyHandler: ProxyHandler) {
    val application = embeddedServer(Netty, 8080) {
        setUpApp(proxyHandler)
    }.start(false)
}

fun runProxyServer(block: ApplicationEngine.() -> Unit) {
    return embeddedServer(Netty, 8081) {
    }.start(false).block()
}
