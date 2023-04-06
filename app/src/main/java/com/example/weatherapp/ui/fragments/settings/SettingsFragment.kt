package com.example.weatherapp.ui.fragments.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.App
import com.example.weatherapp.BuildConfig
import com.example.weatherapp.databinding.FragmentSettingsBinding
import com.example.weatherapp.extention.ViewBindingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : ViewBindingFragment<FragmentSettingsBinding>() {
    val viewMode: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setContent()
        setClickListeners()
    }

    @SuppressLint("SetTextI18n")
    private fun setContent() {
        binding.run {
            userLanguageTV.text =
                "${App.setting.appLanguage.language}, ${App.setting.appLanguage.languageKey}"
            userTempValueTV.text = viewMode.getUnit(requireContext())
            versionAppTV.text = BuildConfig.VERSION_CODE.toString()
        }
    }

    private fun setClickListeners() {
        binding.run {
            backFL.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSettingsBinding = FragmentSettingsBinding.inflate(inflater, container, false)
}