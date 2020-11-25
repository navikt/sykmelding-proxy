package no.nav.sykmelding.proxy

import java.net.URI

private const val PROXY_PREFIX = "PROXY_"

class Environment(envVaribles: Map<String, String> = System.getenv()) {

    val proxyMappings: Map<String, URI> =
            envVaribles.asSequence().filter { it.key.startsWith(PROXY_PREFIX) }.map { getKey(it.key) to getValue(it.value) }.toMap()

    private fun getValue(value: String): URI {
        return URI(value)
    }

    private fun getKey(key: String): String {
        return key.substring(PROXY_PREFIX.length).toLowerCase().replace("_", "-")
    }
}
