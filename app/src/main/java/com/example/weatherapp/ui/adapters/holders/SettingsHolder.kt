package com.example.weatherapp.ui.adapters.holders

import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.App
import com.example.weatherapp.databinding.ItemSettingsElementBinding
import com.example.weatherapp.extention.models.ApplicationLanguage
import com.example.weatherapp.extention.models.ApplicationMetrics
import com.example.weatherapp.setVisibleOrGone

class SettingsHolder(
    private val view: ItemSettingsElementBinding,
    val onItemClicked: (item: Any) -> Unit
) : RecyclerView.ViewHolder(view.root) {
    fun bind(item: Any) {
        view.run {
            val value = if (item is ApplicationLanguage)
                item.language
            else
                (item as ApplicationMetrics).metrics

            nameValueTV.text = value.replaceFirstChar {
                it.uppercase()
            }

            checkedValueIV.setVisibleOrGone(
                if (item is ApplicationLanguage)
                    item.languageKey == App.setting.appLanguage.languageKey
                else
                    (item as ApplicationMetrics).metricsKey == App.setting.appMetrics.metricsKey
            )

            itemView.setOnClickListener {
                onItemClicked(item)
            }
        }
    }
}