package com.example.weatherapp.ui.fragments.settings

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.App
import com.example.weatherapp.BuildConfig
import com.example.weatherapp.R
import com.example.weatherapp.databinding.AlertChangeDialogBinding
import com.example.weatherapp.databinding.FragmentSettingsBinding
import com.example.weatherapp.extention.ViewBindingFragment
import com.example.weatherapp.extention.models.ApplicationLanguage
import com.example.weatherapp.extention.models.ApplicationMetrics
import com.example.weatherapp.ui.adapters.SettingsAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class SettingsFragment : ViewBindingFragment<FragmentSettingsBinding>() {
    private val viewModel: SettingsViewModel by viewModels()

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
            userTempValueTV.text = viewModel.getUnit(requireContext())
            versionAppTV.text = getString(R.string.version_name, BuildConfig.VERSION_NAME)
        }
    }

    private fun setClickListeners() {
        binding.run {
            languageRL.setOnClickListener {
                showSettingsDialog(ApplicationLanguage.getAppLanguageList())
            }

            temperatureRL.setOnClickListener {
                showSettingsDialog(ApplicationMetrics.getMetricsList())
            }

            backFL.setOnClickListener {
                findNavController().popBackStack()
            }

            exitRL.setOnClickListener {
                activity?.finish()
            }
        }
    }

    private fun showSettingsDialog(settings: List<Any>) {
        val dialog = Dialog(ContextThemeWrapper(requireContext(), R.style.CustomAlertDialog))
        val binding: AlertChangeDialogBinding =
            AlertChangeDialogBinding.inflate(LayoutInflater.from(requireContext()))

        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(InsetDrawable(ColorDrawable(Color.TRANSPARENT), 16))


        binding.run {
            closeIV.setOnClickListener {
                dialog.dismiss()
            }

            titleTV.text =
                getString(
                    if (settings.contains(ApplicationLanguage))
                        R.string.lang_app
                    else
                        R.string.temp_app
                )

            settingsRV.adapter = SettingsAdapter(
                settings,
                onItemClicked = {
                    if (it is ApplicationLanguage) {
                        App.setting.appLanguage = it
                        viewModel.changeLocale(
                            requireContext(),
                            Locale(App.setting.appLanguage.languageKey)
                        )
                    } else {
                        App.setting.appMetrics = it as ApplicationMetrics
                    }

                    dialog.dismiss()
                    setContent()
                }
            )
        }

        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSettingsBinding = FragmentSettingsBinding.inflate(inflater, container, false)
}