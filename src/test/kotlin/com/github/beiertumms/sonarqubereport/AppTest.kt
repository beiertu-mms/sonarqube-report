package com.github.beiertumms.sonarqubereport

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.http4k.core.HttpHandler
import org.http4k.core.MemoryResponse
import org.http4k.core.Method
import org.http4k.core.Status
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty
import strikt.assertions.isNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AppTest {

    private val httpClient = mockk<HttpHandler>()
    private val app = App(httpClient)

    @ParameterizedTest
    @MethodSource("provideTestDataForTestParseResultToMarkdownTableRow")
    fun testParseResultToMarkdownTableRow(
        sonarComponent: SonarComponent,
        headerSize: Int,
        expectedOutput: String,
    ) {
        expectThat(
            app.parseResultToMarkdownTableRow(sonarComponent, headerSize)
        ).isEqualTo(expectedOutput)
    }

    private fun provideTestDataForTestParseResultToMarkdownTableRow(): List<Arguments> =
        SonarComponent(
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
        ).let { sonarComponent ->
            listOf(
                Arguments.of(sonarComponent, 3, "|export-service|49|55.9|22|"),
                Arguments.of(sonarComponent, 5, "|export-service|49|55.9|22|n/a|n/a|"),
            )
        }

    @Test
    fun testPrintAsMarkdown() {
        val result = app.printAsMarkdown(
            sonarResult = listOf(
                "|service A|49|55.9|22|",
                "|service B|35|73.9|30|",
            ),
            headers = listOf("complexity", "coverage", "violations")
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

    @Test
    fun testFetchMetricsForSonarComponent() {
        every {
            httpClient(
                any()
            )
        } returns MemoryResponse(Status.OK).body(
            """
                |{
                |  "component": {
                |    "key": "service-key",
                |    "name": "service-key",
                |    "qualifier": "TRK",
                |    "measures": [
                |      {
                |        "metric": "alert_status",
                |        "value": "OK"
                |      },
                |      {
                |        "metric": "coverage",
                |        "value": "55.9",
                |        "bestValue": false
                |      }
                |    ]
                |  }
                |}
            """.trimMargin()
        )

        val result = app.fetchMetricsForSonarComponent(
            "service-key",
            "jwt-session",
            "xsrf-token",
            "http://localhost:0",
            "alert_status,coverage"
        )

        expectThat(result.component.key).isEqualTo("service-key")
        expectThat(result.component.name).isEqualTo("service-key")
        expectThat(result.component.qualifier).isEqualTo("TRK")
        expectThat(result.component.measures).isNotEmpty().hasSize(2)

        result.component.measures.forEach { measure ->
            when (measure.metric) {
                "alert_status" -> expectThat(measure.value).isEqualTo("OK")
                "coverage" -> expectThat(measure.value).isEqualTo("55.9")
                else -> fail("found unexpected metric ${measure.metric}")
            }
        }

        verify(exactly = 1) {
            httpClient(
                withArg { request ->
                    expectThat(request.method).isEqualTo(Method.GET)
                    expectThat("${request.uri}")
                        .isEqualTo(
                            "http://localhost:0/api/measures/component" +
                                "?component=service-key" +
                                "&metricKeys=alert_status%2Ccoverage"
                        )
                    expectThat(request.header("cookie"))
                        .isNotNull()
                        .isEqualTo("XSRF-TOKEN=xsrf-token; JWT-SESSION=jwt-session")
                }
            )
        }
    }
}
