package com.example.weatherapp.extention.models

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

        fun getMetricsList(): List<ApplicationMetrics> {
            return listOf(
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
