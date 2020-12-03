package com.example.weatherapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.api.API
import com.example.weatherapp.dataClasses.Weather
import com.example.weatherapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*binding.editTextCityName.doOnTextChanged { text, _, _, _ ->
            toast(text.toString())
        }*/
        getWeatherForecast()
    }

    @SuppressLint("SetTextI18n")
    private fun onDataLoaded(weather: Weather?) {
        if (weather != null) {
            binding.apply {
                city.setText(weather.name)
                country.text = "${weather.sys?.country}"
                updatedAt.text = "Updated at: ${weather.dt?.toFormattedDate("dd/MM/yyyy hh:mm a")}"
                status.text = weather.weather!![0].description.capitalize()
                temp.text = "${(weather.main?.temp!!).roundToInt()}°C"
                tempMin.text = "Min Temp: ${weather.main.temp_min.toInt()}°C"
                tempMax.text = "Max Temp: ${(weather.main.temp_max).roundToInt()}°C"
                sunrise.text = weather.sys?.sunrise?.toFormattedDate("hh:mm a")
                sunset.text = weather.sys?.sunset?.toFormattedDate("hh:mm a")
                wind.text = weather.wind?.speed.toString()
                pressure.intData = weather.main.pressure
                humidity.text = weather.main.humidity.toString()
            }
        }
    }

    private fun getWeatherForecast() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val answer = API.api.getWeather("Donetsk").execute()
                launch(Dispatchers.Main) { onDataLoaded(answer.body()) }
            } catch (e: Exception) {
                launch(Dispatchers.Main) { onDataLoaded(null) }
            }
        }

        API.api.getWeather("Donetsk").enqueue(object : Callback<Weather> {
            override fun onFailure(call: Call<Weather>, t: Throwable) {}
            override fun onResponse(call: Call<Weather>, response: Response<Weather>) {
                response.body()?.let { weather -> onDataLoaded(weather) }
            }
        })
    }

    fun Int.toFormattedDate(format: String): String {
        val formatter = SimpleDateFormat(format, Locale.ENGLISH)
        return formatter.format(Date(this * 1000L))
    }

    var TextView.intData: Int?
        get() = text.toString().toIntOrNull()
        set(value) {
            text = value.toString()
        }
}
//onboarding
//viewpager2