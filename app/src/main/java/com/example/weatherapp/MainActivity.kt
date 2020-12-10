package com.example.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.R.layout
import com.example.weatherapp.api.API
import com.example.weatherapp.dataClasses.Weather
import com.example.weatherapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val UACities = arrayOf("Aberdeen", "Aldershot", "Altrincham", "Ashford", "Atherton", "Aylesbury", "Bamber Bridge", "Bangor", "Barnsley", "Barry", "Basildon", "Basingstoke", "Bath", "Batley", "Bebington", "Bedford", "Beeston", "Belfast", "Birkenhead", "Birmingham", "Blackburn", "Blackpool", "Bloxwich", "Bognor Regis", "Bolton", "Bootle", "Bournemouth", "Bracknell", "Bradford", "Brentwood", "Brighton and Hove", "Bristol", "Burnley", "Burton upon Trent", "Bury", "Cambridge", "Milton", "Cannock", "Canterbury", "Cardiff", "Carlisle", "Carlton", "Chatham", "Chelmsford", "Cheltenham", "Chester", "Chesterfield", "Christchurch", "Clacton-on-Sea", "Colchester", "Corby", "Coventry", "Craigavon", "incl. Lurgan, Portadown", "Crawley", "Crewe", "Crosby", "Cumbernauld", "Darlington", "Derby", "Derry", "Londonderry", "Dewsbury", "Doncaster", "Dudley", "Dundee", "Dunfermline", "Durham", "Eastbourne", "East Kilbride", "Eastleigh", "Edinburgh", "Ellesmere Port", "Esher", "Ewell", "Exeter", "Farnborough", "Filton", "Folkestone", "Gateshead", "Gillingham", "Glasgow", "Gloucester", "Gosport", "Gravesend", "Grays", "Grimsby", "Guildford", "Halesowen", "Halifax", "Hamilton", "Harlow", "Harrogate", "Hartlepool", "Hastings", "Hemel Hempstead", "Hereford", "High Wycombe", "Horsham", "Huddersfield", "Ipswich", "Keighley", "Kettering", "Kidderminster", "Kingston upon Hull", "Hull", "Kingswinford", "Kirkcaldy", "Lancaster", "Leeds", "Leicester", "Lincoln", "Littlehampton", "Liverpool", "Livingston", "London", "Loughborough", "Lowestoft", "Luton", "Macclesfield", "Maidenhead", "Maidstone", "Manchester", "Mansfield", "Margate", "Middlesbrough", "Milton Keynes", "Neath", "Newcastle", "Newcastle upon Tyne", "Newcastle-under-Lyme", "Newport", "Newtownabbey", "Northampton", "Norwich", "Nottingham", "Nuneaton", "Oldham", "Oxford", "Paignton", "Paisley", "Peterborough", "Plymouth", "Poole", "Portsmouth", "Preston", "Rayleigh", "Reading", "Redditch", "Rochdale", "Rochester", "Rotherham", "Royal Leamington Spa", "Royal Tunbridge Wells", "Rugby", "Runcorn", "Sale", "Salford", "Scarborough", "Scunthorpe", "Sheffield", "Shoreham-by-Sea", "Shrewsbury", "Sittingbourne", "Slough", "Smethwick", "Solihull", "Southampton", "Southend-on-Sea", "Southport", "South Shields", "Stafford", "St Albans", "Stevenage", "St Helens", "Stockport", "Stockton-on-Tees", "Stoke-on-Trent", "Stourbridge", "Sunderland", "Sutton Coldfield", "Swansea", "Swindon", "Tamworth", "Taunton", "Telford", "Torquay", "Tynemouth", "Wakefield", "Wallasey", "Walsall", "Walton-on-Thames", "Warrington", "Washington", "Watford", "Wellingborough", "Welwyn Garden City", "West Bromwich", "Weston-super-Mare", "Weymouth", "Widnes", "Wigan", "Willenhall", "Woking", "Wolverhampton", "Worcester", "Worthing", "Wrexham", "York")

        val adapter = ArrayAdapter<String>(this, layout.support_simple_spinner_dropdown_item, UACities)
        binding.city.setAdapter(adapter)

        //Минимальное число символов для показа выпадающего списка
        binding.city.threshold = 2

        binding.city.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val selItem = parent.getItemAtPosition(position).toString()
            getWeatherForecast(selItem)
        }

        binding.city.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                binding.city.showDropDown()
        }

        binding.synchronizeImageButton.setOnClickListener {
            getWeatherForecast(binding.city.text.toString())
        }

        val city = binding.city.text.toString()

        getWeatherForecast(city)

        val pref = getSharedPreferences("setting", Context.MODE_PRIVATE)
        pref.edit()
                .putString("language", "ru")
                .apply()
    }

    @SuppressLint("SetTextI18n")
    private fun onDataLoaded(weather: Weather?) {
        if (weather != null) {
            binding.apply {
                city.clearFocus()
                city.setText(weather.name.toString().toUpperCase())
                country.text = "${weather.sys?.country}"
                updatedAt.text = getString(R.string.update_data, "${weather.dt?.toFormattedDate("dd/MM/yyyy hh:mm a")}")
                //updatedAt.text = "Updated at: ${weather.dt?.toFormattedDate("dd/MM/yyyy hh:mm a")}"
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

    private fun getWeatherForecast(cityName : String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val answer = API.api.getWeather(cityName).execute()
                launch(Dispatchers.Main) {
                    onDataLoaded(answer.body())
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    onDataLoaded(null)
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
//localization

//dagger
//rxjava
//realm
//https://github.com/DingMouRen/LayoutManagerGroup
//https://github.com/dm77/barcodescanner
//https://github.com/florent37/RuntimePermission