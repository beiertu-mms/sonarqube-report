package com.github.beiertumms.sonarqubereport

import com.fasterxml.jackson.annotation.JsonProperty

internal data class SonarComponent(
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
    ) {
        fun sortedMeasures() = measures.sortedBy { it.metric }
    }

    data class Measure(
        @field:JsonProperty("metric")
        val metric: String,
        @field:JsonProperty("value")
        val value: String,
    )
}
