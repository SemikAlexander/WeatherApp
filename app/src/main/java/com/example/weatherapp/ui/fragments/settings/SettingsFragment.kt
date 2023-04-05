package com.example.weatherapp.ui.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.databinding.FragmentSettingsBinding
import com.example.weatherapp.extention.ViewBindingFragment

class SettingsFragment : ViewBindingFragment<FragmentSettingsBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setClickListeners()
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