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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pref = getSharedPreferences("setting", Context.MODE_PRIVATE)
        val lanStr = pref.getString("language", null)
        var setLanguage = ""
        val map = mapOf("Español" to "es", "English" to "en", "Русский" to "ru")
        val values = map.values.toList()
        val keys = map.keys

        val adapter = ArrayAdapter(this, R.layout.spinner_item, keys.toMutableList())

        binding.apply {
            lanSpinner.adapter = adapter
            lanSpinner.setSelection(values.indexOf(lanStr))
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

            saveButton.setOnClickListener {
                if(setLanguage != "") {
                    var editor = pref.edit()
                    editor.putString("language", setLanguage)
                    editor.apply()
                }
                startActivity<SplashActivity>()
                finish()
            }
        }
    }

    override fun onBackPressed() {
        //startActivity<MainActivity>()
        finish()
    }
}