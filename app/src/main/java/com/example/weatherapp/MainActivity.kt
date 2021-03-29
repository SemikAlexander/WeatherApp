package com.example.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.weatherapp.R.layout
import com.example.weatherapp.api.API
import com.example.weatherapp.citiesDataClasses.Cities
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.weatherDataClasses.Weather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    companion object {
        private const val DELAY = 1000L
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*val pref = getSharedPreferences("setting", Context.MODE_PRIVATE)

        if (pref.getString("mode", null) == "dark")
            setTheme(R.style.Theme_WeatherAppNight)
        else
            setTheme(R.style.Theme_WeatherApp)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {

            city.threshold = 2

            synchronizeImageButton.setOnClickListener {
                getWeatherForecast(binding.city.text.toString())
                toast(getString(R.string.change_data))
            }

            var timer = Timer()
            city.doAfterTextChanged {
                timer.cancel()
                timer = Timer()
                timer.schedule(
                        object : TimerTask() {
                            override fun run() {
                                getCity(it.toString())
                            }
                        },
                        DELAY
                )
            }

            settingImageButton.setOnClickListener {
                startActivity<SettingsActivity>()
            }
        }

        getWeatherForecast(binding.city.text.toString())*/
    }

    override fun onStart() {
        val pref = getSharedPreferences("setting", Context.MODE_PRIVATE)

        if (pref.getString("mode", null) == "dark")
            setTheme(R.style.Theme_WeatherAppNight)
        else
            setTheme(R.style.Theme_WeatherApp)

        super.onStart()
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {

            city.threshold = 2

            synchronizeImageButton.setOnClickListener {
                getWeatherForecast(binding.city.text.toString())
                toast(getString(R.string.change_data))
            }

            var timer = Timer()
            city.doAfterTextChanged {
                timer.cancel()
                timer = Timer()
                timer.schedule(
                        object : TimerTask() {
                            override fun run() {
                                getCity(it.toString())
                            }
                        },
                        DELAY
                )
            }

            settingImageButton.setOnClickListener {
                startActivity<SettingsActivity>()
            }
        }

        getWeatherForecast(binding.city.text.toString())
    }

    @SuppressLint("SetTextI18n")
    private fun weatherDataLoaded(weather: Weather?) {
        if (weather != null) {
            binding.apply {
                city.clearFocus()
                city.setText(weather.name.toString().toUpperCase())
                country.text = "${weather.sys?.country}"
                updatedAt.text = getString(R.string.update_data, "${weather.dt?.toFormattedDate("dd/MM/yyyy hh:mm a")}")
                status.text = weather.weather!![0].description.capitalize()
                temp.text = "${(weather.main?.temp!!).roundToInt()}°C"
                tempMin.text = getString(R.string.min_temp, "${weather.main.temp_min.toInt()}°C")
                tempMax.text = getString(R.string.max_temp, "${weather.main.temp_max.toInt()}°C")
                sunrise.text = weather.sys?.sunrise?.toFormattedDate("hh:mm a")
                sunset.text = weather.sys?.sunset?.toFormattedDate("hh:mm a")
                wind.text = weather.wind?.speed.toString()
                pressure.intData = weather.main.pressure
                humidity.text = weather.main.humidity.toString()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun cityDataLoaded(cities: Cities?) {
        if (cities != null) {
            binding.apply {
                val citiesNames = cities.map { it.title }.toMutableList()
                city.setAdapter(ArrayAdapter(
                        this@MainActivity,
                        layout.support_simple_spinner_dropdown_item,
                        citiesNames
                ))

                city.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
                    getWeatherForecast(parent.getItemAtPosition(position).toString())
                }
            }
        }
    }

    private fun getWeatherForecast(cityName: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val answer = API.api.getWeather(cityName).execute()
                launch(Dispatchers.Main) {
                    weatherDataLoaded(answer.body())
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    weatherDataLoaded(null)
                }
            }
        }
    }

    private fun getCity(cityName: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val answer = API.api2.getCities(cityName).execute()
                launch(Dispatchers.Main) {
                    cityDataLoaded(answer.body())
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    cityDataLoaded(null)
                }
            }
        }
    }

    private fun Long.toFormattedDate(format: String): String {
        val formatter = SimpleDateFormat(format, Locale.ENGLISH)
        return formatter.format(Date(this * 1000L))
    }

    private var TextView.intData: Int?
        get() = text.toString().toIntOrNull()
        set(value) {
            text = value.toString()
        }
}
//swipe=refresh-layout
//onboarding
//viewpager2
//localization [+]

//dagger
//rxjava
//realm
//https://github.com/DingMouRen/LayoutManagerGroup
//https://github.com/dm77/barcodescanner
//https://github.com/florent37/RuntimePermission