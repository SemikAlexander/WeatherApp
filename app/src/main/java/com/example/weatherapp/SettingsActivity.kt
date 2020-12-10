package com.example.weatherapp

import android.content.Context
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pref = getSharedPreferences("setting", Context.MODE_PRIVATE)
        var lan: String = ""
        val map = mapOf("Español" to "es", "English" to "en", "Русский" to "ru")

        binding.apply {
            val lanStr = pref.getString("language", null)
            if (lanStr != null)
            {
                if (lanStr == "en")
                    enRadioButton.isChecked = true
                else if(lanStr == "ru")
                    ruRadioButton.isChecked = true
                else
                    esRadioButton.isChecked = true
            }

            radioGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
                val radio: RadioButton = findViewById(checkedId)
                lan = radio.text.toString()
            })

            saveButton.setOnClickListener {

                var editor = pref.edit()
                editor.putString("language", map.getValue(lan))
                editor.apply()

                startActivity<SplashActivity>()
                finish()
            }
        }

    }
}