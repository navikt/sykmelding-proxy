package no.nav.sykmelding.proxy

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ProxyHandlerTest : Spek({
    val environment = Environment(mapOf("PROXY_TEST_API" to "http://localhost:8081"))
    val httpClient = HttpClient()

    val proxyHandler = ProxyHandler(environment.proxyMappings, HttpClient())


    runTestServer(proxyHandler)

    runProxyServer {
        application.install(Routing) {
            setUpTestRoutes()
        }
    }

    describe("No proxy") {
        it("is_alive") {
            runBlocking {
                val response = httpClient.get<HttpResponse>("http://localhost:8080/is_alive")
                assertEquals(HttpStatusCode.OK, response.status)
                assertEquals("Alive!", response.receive())
            }
        }
        it("is_ready") {
            runBlocking {
                val response = httpClient.get<HttpResponse>("http://localhost:8080/is_ready")
                assertEquals(HttpStatusCode.OK, response.status)
                assertEquals("Ready!", response.receive())
            }
        }
        it("prometheus") {
            runBlocking {
                val response = httpClient.get<HttpResponse>("http://localhost:8080/prometheus")
                assertEquals(HttpStatusCode.OK, response.status)
                val reponseText = response.receive<String>()
                assertTrue { reponseText.length > 100 }
            }
        }
    }

    describe("Test proxy") {
        it("Should proxy simple get") {
            val url = "http://localhost:8080/test-api"
            runBlocking {
                val response = httpClient.get<HttpResponse>(url)
                assertEquals("", response.readText())
                assertEquals(HttpStatusCode.OK, response.status)
            }
        }
        it("Should proxy simple get with path param") {
            val url = "http://localhost:8080/test-api/with/1"
            runBlocking {
                val response = httpClient.get<HttpResponse>(url)
                assertEquals("OK + 1", response.readText())
                assertEquals(HttpStatusCode.OK, response.status)
                assertEquals(ContentType.Text.Plain, response.contentType())
            }
        }
        it("Should proxy simple get with path param and query param") {
            val url = "http://localhost:8080/test-api/with/1?param=2"
            runBlocking {
                val response = httpClient.get<HttpResponse>(url)
                assertEquals("OK + 1 + 2", response.readText())
                assertEquals(HttpStatusCode.OK, response.status)
                assertEquals(ContentType.Text.Plain, response.contentType())
            }
        }

        it("Test get JSON") {
            val url = "http://localhost:8080/test-api/getjson"
            runBlocking {
                val response = httpClient.get<HttpResponse>(url)
                assertEquals("{\"key\": \"value\"}", response.readText())
                assertEquals(HttpStatusCode.OK, response.status)
                assertEquals(ContentType.Application.Json, response.contentType())
            }
        }
        it("Test get on post endpoint should fail") {
            val url = "http://localhost:8080/test-api/postjson"
            runBlocking {
                val response = httpClient.get<HttpResponse>(url)
                assertEquals(HttpStatusCode.NotFound, response.status)
            }
        }

        it("Test post endpoint") {
            val url = "http://localhost:8080/test-api/postjson"
            runBlocking {
                val postBody = "{\"key\": \"value\"}"
                val response = httpClient.post<HttpResponse>(url) {
                    body = postBody
                }
                assertEquals(HttpStatusCode.OK, response.status)
                assertEquals(postBody, response.receive())
            }
        }

        it("InternalServerError") {
            val url = "http://localhost:8080/test-api/error"
            runBlocking {
                val response = httpClient.get<HttpResponse>(url)
                assertEquals(HttpStatusCode.InternalServerError, response.status)
            }
        }

        it("test auth ok") {
            val url = "http://localhost:8080/test-api/withauth"
            runBlocking {
                val response = httpClient.get<HttpResponse>(url) {
                    header(HttpHeaders.Authorization, "Bearer token")
                }
                assertEquals(HttpStatusCode.OK, response.status)
            }
        }
        it("test auth not ok") {
            val url = "http://localhost:8080/test-api/withauth"
            runBlocking {
                val response = httpClient.get<HttpResponse>(url)
                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }
        }
    }
})

private fun Routing.setUpTestRoutes() {
    get("") {
        call.respond(HttpStatusCode.OK)
    }

    get("/with/{id}") {
        val id = call.parameters["id"]
        val param = call.parameters["param"]
        val stringBuilder = StringBuilder()
        stringBuilder.append("OK")
        if(id != null) stringBuilder.append(" + $id")
        if(param != null) stringBuilder.append(" + $param")
        call.respondBytes(contentType = ContentType.Text.Plain, status = HttpStatusCode.OK, bytes = stringBuilder.toString().toByteArray())
    }

    get("/getjson") {
        call.respondBytes(contentType = ContentType.Application.Json, status = HttpStatusCode.OK, bytes = "{\"key\": \"value\"}".toByteArray())
    }

    post("/postjson") {
        val body = call.receive<ByteArray>()
        call.respondBytes(contentType = ContentType.Application.Json, status = HttpStatusCode.OK, bytes = body)
    }

    get("/error") {
        call.respond(HttpStatusCode.InternalServerError)
    }

    get("/withauth") {
        val bearerToken = call.request.header("Authorization")
        if(bearerToken == "Bearer token") {
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.Unauthorized)
        }
    }
}
