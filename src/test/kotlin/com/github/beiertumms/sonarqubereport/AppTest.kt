package com.github.beiertumms.sonarqubereport

import io.mockk.mockk
import org.http4k.core.HttpHandler
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class AppTest {

    private val httpClient = mockk<HttpHandler>()
    private val app = App(httpClient)

    @Test
    fun testParseResultToMarkdownTableRow() {
        val sonarComponent = SonarComponent(
            component = SonarComponent.Component(
                key = "export-service",
                name = "export-service",
                qualifier = "TRK",
                measures = listOf(
                    SonarComponent.Measure(
                        metric = "coverage",
                        value = "55.9"
                    ),
                    SonarComponent.Measure(
                        metric = "violations",
                        value = "22"
                    ),
                    SonarComponent.Measure(
                        metric = "complexity",
                        value = "49"
                    ),
                ),
            )
        )

        val result = app.parseResultToMarkdownTableRow(sonarComponent)

        expectThat(result).isEqualTo("|export-service|49|55.9|22|")
    }

    @Test
    fun testPrintAsMarkdown() {
        val result = app.printAsMarkdown(
            sonarResult = listOf(
                "|service A|49|55.9|22|",
                "|service B|35|73.9|30|",
            ),
            keys = "coverage,violations,complexity"
        )

        expectThat(result).isEqualTo(
            """
                |# SonarQube Report
                |
                |See [SonarQube metrics](https://docs.sonarqube.org/latest/user-guide/metric-definitions/) for more information.
                |
                ||key|complexity|coverage|violations|
                ||---|:---:|:---:|:---:|
                ||service A|49|55.9|22|
                ||service B|35|73.9|30|
            """.trimMargin()
        )
    }
}
