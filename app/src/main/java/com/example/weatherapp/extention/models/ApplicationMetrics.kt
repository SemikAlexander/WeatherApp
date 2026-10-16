package com.example.weatherapp.extention.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ApplicationMetrics(
    val metrics: String,
    val metricsKey: String
) {
    companion object {
        fun setDefaultAppMetrics(): ApplicationMetrics {
            return ApplicationMetrics(
                metrics = "standard",
                metricsKey = "K"
            )
        }

        fun getMetricsList(): ImmutableList<ApplicationMetrics> {
            return persistentListOf(
                ApplicationMetrics(
                    metrics = "standard",
                    metricsKey = "K"
                ),
                ApplicationMetrics(
                    metrics = "metric",
                    metricsKey = "C"
                ),
                ApplicationMetrics(
                    metrics = "imperial",
                    metricsKey = "F"
                )
            )
        }
    }
}
