package com.example.weatherapp.ui.fragments.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.api.contollers.CityServiceImpl
import com.example.weatherapp.api.contollers.WeatherServiceImpl
import com.example.weatherapp.api.models.response.cities.Cities
import com.example.weatherapp.api.models.response.weather.Weather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    var cityName = "London"

    private val _weatherData = MutableSharedFlow<Weather?>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    val weatherData: SharedFlow<Weather?> = _weatherData.asSharedFlow()

    fun getWeather() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                WeatherServiceImpl().getWeather(cityName)
            }
                .onFailure {
                    _weatherData.emit(null)
                }
                .onSuccess {
                    _weatherData.emit(it)
                }
        }
    }

    private val _cityData = MutableSharedFlow<Cities?>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    val cityData: SharedFlow<Cities?> = _cityData.asSharedFlow()

    fun getCity() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                CityServiceImpl().getCity(cityName)
            }
                .onFailure {
                    _cityData.emit(null)
                }
                .onSuccess {
                    _cityData.emit(it)
                }
        }
    }
}