package com.github.beiertumms.sonarqubereport

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import org.http4k.client.ApacheClient
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.format.Jackson.auto
import java.io.File

fun main(args: Array<String>) = App().main(args)

// See https://ajalt.github.io/clikt/ for documentation
class App(
    private val httpClient: HttpHandler = ApacheClient()
) : CliktCommand(
    name = "java -jar app.jar",
    help = "A simple cli to fetch and print SonarQube metric(s) for the given component(s). (*) = required",
    printHelpOnEmptyArgs = true,
) {

    private val sonarQubeUrl by option(
        help = "the base url to the SonarQube instance. Default: https://sonarqube.cloud.mmst.eu"
    ).default("https://sonarqube.cloud.mmst.eu")

    private val jwtSession by option(
        help = "(*) the JWT"
    )

    private val xsrfToken by option(
        help = "(*) the XSRF token"
    )

    private val componentKeys by option(
        help = "comma separated list of SonarQube component keys. Default: empty string"
    ).default("")

    private val metricKeys by option(
        help = "comma separated list of SonarQube metrics. " +
            "Default: alert_status,coverage,complexity,violations,vulnerabilities,security_rating,security_hotspots"
    ).default("alert_status,coverage,complexity,violations,vulnerabilities,security_rating,security_hotspots")

    private val sonarComponentLens = Body.auto<SonarComponent>().toLens()

    override fun run() {
        val headers = metricKeys
            .split(",")
            .map(String::trim)
            .filter(String::isNotBlank)
            .sorted()

        componentKeys.split(",")
            .map(String::trim)
            .filter(String::isNotBlank)
            .map { fetchMetricsForSonarComponent(it, requireNotNull(jwtSession), requireNotNull(xsrfToken)) }
            .map { parseResultToMarkdownTableRow(it, headers.size) }
            .let { sonarResult ->
                File("./sonar-report.md")
                    .writeText(printAsMarkdown(sonarResult, headers))
            }
    }

    internal fun fetchMetricsForSonarComponent(
        componentKey: String,
        jwt: String,
        token: String,
        sonarUrl: String = sonarQubeUrl,
        metrics: String = metricKeys,
    ): SonarComponent = try {
        Request(Method.GET, "$sonarUrl/api/measures/component")
            .query("component", componentKey)
            .query("metricKeys", metrics)
            .header("cookie", "XSRF-TOKEN=$token; JWT-SESSION=$jwt")
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

    internal fun parseResultToMarkdownTableRow(sonarComponent: SonarComponent, headerSize: Int): String = sonarComponent.component.let {
        val str = listOf(it.key)
            .plus(it.sortedMeasures().map(SonarComponent.Measure::value))
            .joinToString("|")

        val diff = headerSize.minus(it.measures.size)
        if (diff == 0) "|$str|" else "|$str|".plus("n/a|".repeat(diff))
    }

    internal fun printAsMarkdown(sonarResult: List<String>, headers: List<String>): String {
        return listOf(
            "# SonarQube Report\n",
            "See [SonarQube metrics](https://docs.sonarqube.org/latest/user-guide/metric-definitions/) " +
                "for more information.\n",
            "|key|${headers.joinToString("|")}|",
            "|---|${headers.joinToString(separator = "") { ":---:|" }}",
        ).plus(sonarResult).joinToString("\n")
    }
}
