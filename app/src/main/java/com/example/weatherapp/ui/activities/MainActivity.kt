package com.example.weatherapp.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.R.layout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)
    }
}