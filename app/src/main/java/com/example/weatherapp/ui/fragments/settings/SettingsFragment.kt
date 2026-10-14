package com.example.weatherapp.ui.fragments.settings

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.weatherapp.App
import com.example.weatherapp.BuildConfig
import com.example.weatherapp.R
import com.example.weatherapp.databinding.AlertChangeDialogBinding
import com.example.weatherapp.databinding.FragmentSettingsBinding
import com.example.weatherapp.extention.ViewBindingFragment
import com.example.weatherapp.extention.models.ApplicationLanguage
import com.example.weatherapp.extention.models.ApplicationMetrics
import com.example.weatherapp.ui.MainActivity
import com.example.weatherapp.ui.adapters.SettingsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class SettingsFragment : ViewBindingFragment<FragmentSettingsBinding>() {
    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setContent()
        setClickListeners()
        observeRestartEvent()
    }

    /**
     * Реакция на смену языка: перезапуск единственной Activity
     * с новой локалью (она применится в attachBaseContext).
     * CLEAR_TASK + NEW_TASK нужны, чтобы полностью очистить back stack навграфа;
     * launchMode="singleTop" в манифесте гарантирует, что новый экземпляр не задвоится.
     */
    private fun observeRestartEvent() {
        viewModel.restartEvent
            .onEach {
                val context = requireContext().applicationContext
                val intent = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra(MainActivity.EXTRA_RESTART_FOR_LOCALE, true)
                }
                context.startActivity(intent)
            }
            .launchIn(lifecycleScope)
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
                        viewModel.changeLocale()
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