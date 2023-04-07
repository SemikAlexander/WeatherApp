package com.example.weatherapp.ui.activities

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.example.weatherapp.App
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.startCheckingConnection
import com.example.weatherapp.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        val appLanguage = MutableLiveData(App.setting.appLanguage.languageKey.lowercase())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setLocale(this, appLanguage.value.toString())
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        startCheckingConnection(applicationContext) {
            CoroutineScope(Dispatchers.Main).launch {
                binding.run {
                    toast(getString(R.string.internet_error))
                }
            }
        }
    }

    private fun setLocale(context: Context, languageKey: String) {
        val config = Configuration()
        val locale = Locale(languageKey)

        config.setLocale(locale)
        context.createConfigurationContext(config)
    }
}