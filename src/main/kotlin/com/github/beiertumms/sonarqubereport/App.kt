package com.github.beiertumms.sonarqubereport

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import org.http4k.client.ApacheClient
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.format.Jackson.auto

fun main(args: Array<String>) = App().main(args)

// See https://ajalt.github.io/clikt/ for documentation
class App : CliktCommand(
    name = "java -jar app.jar",
    help = "A simple template to create cli. (*) = required",
    printHelpOnEmptyArgs = true,
) {

    private val sonarQubeUrl by option(
        help = "the base url to the SonarQube instance"
    ).default("https://sonarqube.cloud.mmst.eu")

    private val jwtSession by option(
        help = "the JWT"
    )

    private val xsrfToken by option(
        help = "the XSRF token"
    )

    private val componentKeys by option(
        help = "comma separated list of SonarQube component keys"
    ).default("")

    private val httpClient = ApacheClient()
    private val sonarComponentLens = Body.auto<SonarComponent>().toLens()

    override fun run() {
        componentKeys.split(",")
            .map(String::trim)
            .filter(String::isNotBlank)
            .map { fetchMetricsForSonarComponent(it) }
            .forEach(::println)
    }

    private fun fetchMetricsForSonarComponent(
        componentKey: String,
        metricKeys: String = "coverage,complexity,violations",
    ): SonarComponent = try {
        Request(Method.GET, "$sonarQubeUrl/api/measures/component")
            .query("component", componentKey)
            .query("metricKeys", metricKeys)
            .header("cookie", "XSRF-TOKEN=$xsrfToken; JWT-SESSION=$jwtSession")
            .let(httpClient::invoke)
            .let(::handleSonarApiResponse)
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }

    private fun handleSonarApiResponse(response: Response) = when {
        response.status.successful -> sonarComponentLens(response)
        else -> throw IllegalStateException("failed to fetch SonarQube metrics, cause: $response")
    }
}

private data class SonarComponent(
    @field:JsonProperty("component")
    val component: Component,
) {
    data class Component(
        @field:JsonProperty("key")
        val key: String,
        @field:JsonProperty("name")
        val name: String,
        @field:JsonProperty("qualifier")
        val qualifier: String,
        @field:JsonProperty("measures")
        val measures: List<Measure>,
    )

    data class Measure(
        @field:JsonProperty("metric")
        val metric: String,
        @field:JsonProperty("value")
        val value: String,
    )
}
