package com.example.weatherapp.ui.fragments.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.App
import com.example.weatherapp.R
import com.example.weatherapp.api.models.response.cities.Cities
import com.example.weatherapp.api.models.response.weather.Weather
import com.example.weatherapp.databinding.FragmentHomeBinding
import com.example.weatherapp.extention.ViewBindingFragment
import com.example.weatherapp.setVisibleOrGone
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@AndroidEntryPoint
class HomeFragment : ViewBindingFragment<FragmentHomeBinding>() {
    private val viewModel: HomeViewModel by viewModels()
    private val DELAY = 1000L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.weatherData.onEach { weather ->
            if (weather != null) {
                loadingStatus(false)
                setWeatherContent(weather)
            }
        }.launchIn(lifecycleScope)

        viewModel.cityData.onEach { cities ->
            if (cities != null) {
                setCityContent(cities)
            }
        }.launchIn(lifecycleScope)

        binding.cityACTV.threshold = 2

        getWeatherData()
        setClickListeners()
    }

    private fun getWeatherData() {
        loadingStatus(true)
        viewModel.getWeather()
    }

    @SuppressLint("SetTextI18n")
    private fun setWeatherContent(weather: Weather) {
        binding.run {
            val units = when (App.setting.appMetrics.metrics) {
                "standard" -> "K"
                "metric" -> "C"
                else -> "F"
            }

            cityACTV.clearFocus()
            cityACTV.setText(weather.name.toString())
            countryTV.text = "${weather.sys?.country}"
            updatedAtTV.text = getString(
                R.string.update_data,
                "${weather.dt?.toFormattedDate("dd/MM/yyyy hh:mm a")}"
            )

            statusTV.text = weather.weather!![0].description.replaceFirstChar {
                it.uppercase()
            }
            temperatureTV.text = "${(weather.main?.temp!!).roundToInt()}°$units"
            tempMinTV.text = getString(R.string.min_temp, "${weather.main.temp_min.toInt()}°$units")
            tempMaxTV.text = getString(R.string.max_temp, "${weather.main.temp_max.toInt()}°$units")
            sunriseTV.text = weather.sys?.sunrise?.toFormattedDate("hh:mm a")
            sunsetTV.text = weather.sys?.sunset?.toFormattedDate("hh:mm a")
            windTV.text = weather.wind?.speed.toString()
            pressureTV.text = weather.main.pressure.toString()
            humidityTV.text = weather.main.humidity.toString()
        }
    }

    private fun setCityContent(cities: Cities) {
        binding.run {
            val citiesNames = cities.map { it.title }.toMutableList()
            cityACTV.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.support_simple_spinner_dropdown_item,
                    citiesNames
                )
            )

            cityACTV.onItemClickListener =
                AdapterView.OnItemClickListener { parent, _, position, _ ->
                    viewModel.cityName = parent.getItemAtPosition(position).toString()
                    viewModel.getWeather()
                }
        }
    }

    private fun setClickListeners() {
        binding.run {
            var timer = Timer()

            refreshSRL.setOnRefreshListener {
                loadingStatus(true)
                viewModel.getWeather()
            }

            settingsIV.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
            }

            cityACTV.doAfterTextChanged {
                timer.cancel()
                timer = Timer()
                timer.schedule(
                    object : TimerTask() {
                        override fun run() {
                            viewModel.cityName = it.toString()
                            viewModel.getCity()
                        }
                    },
                    DELAY
                )
            }
        }
    }

    private fun loadingStatus(isLoading: Boolean) {
        binding.run {
            loaderFL.setVisibleOrGone(isLoading)
            refreshSRL.isRefreshing = false
        }
    }

    private fun Long.toFormattedDate(format: String): String {
        val formatter = SimpleDateFormat(format, Locale.ENGLISH)
        return formatter.format(Date(this * 1000L))
    }

    override fun makeBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHomeBinding.inflate(inflater, container, false)
}