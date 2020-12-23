package com.example.weatherapp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    var setLanguage = ""
    var setMode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = getSharedPreferences("setting", Context.MODE_PRIVATE)
        setMode = pref.getString("mode", null).toString()
        if (setMode == "dark")
            setTheme(R.style.Theme_WeatherAppNight)
        else
            setTheme(R.style.Theme_WeatherApp)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val map = mapOf("Español" to "es", "English" to "en", "Русский" to "ru")
        val values = map.values.toList()
        val keys = map.keys

        binding.apply {
            themeSwitch.isChecked = setMode == "dark"

            lanSpinner.adapter = ArrayAdapter(this@SettingsActivity, R.layout.spinner_item, keys.toMutableList())
            lanSpinner.setSelection(values.indexOf(pref.getString("language", null)))

            lanSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                ) {
                    setLanguage = values[position]
                }
            }

            themeSwitch.setOnCheckedChangeListener { _, isChecked ->
                setMode = if (isChecked)
                    "dark"
                else
                    "day"
            }

            saveButton.setOnClickListener {
                if (setLanguage != "") {
                    val editor = pref.edit()
                    editor.putString("language", setLanguage)
                    editor.putString("mode", setMode)
                    editor.apply()
                }
                startActivity<SplashActivity>()
                finish()
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }
}