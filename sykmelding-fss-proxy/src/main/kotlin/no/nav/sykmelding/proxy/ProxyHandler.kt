package no.nav.sykmelding.proxy

import io.ktor.application.ApplicationCall
import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import io.ktor.http.HttpHeaders.isUnsafe
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.request.contentType
import io.ktor.request.httpMethod
import io.ktor.request.path
import io.ktor.request.receiveOrNull
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.util.filter
import java.net.URI

class ProxyHandler(val proxyMapping: Map<String, URI>, private val httpClient: HttpClient) {
    suspend fun handleProxyEvent(call: ApplicationCall) {
        val proxyApi = call.request.path().split("/")[1]
        val proxyPath = call.request.uri.substring(proxyApi.length + 1)
        if(proxyMapping.containsKey(proxyApi)) {
            val proxyHeaders = call.request.headers.filter { key, _ -> !isUnsafe(key) && key != "x-nav-apikey" }
            val proxyBody = call.receiveOrNull<ByteArray>()
            val proxyMethod = call.request.httpMethod
            val url = proxyMapping[proxyApi].toString() + proxyPath
            val response = httpClient.request<HttpResponse>(urlString = url) {
                method = proxyMethod
                headers.appendAll(proxyHeaders)
                if(proxyBody != null) {
                    body = proxyBody
                    contentType(call.request.contentType())
                }
            }
            call.respondBytes(contentType = response.contentType(), status = response.status, bytes = response.readBytes())
        } else {
            call.respond(HttpStatusCode.BadGateway, "Application $proxyApi not configured")
        }
    }
}
