package com.example.weatherapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ItemSettingsElementBinding
import com.example.weatherapp.extention.models.ApplicationLanguage
import com.example.weatherapp.ui.adapters.holders.SettingsHolder

class SettingsAdapter(
    var items: List<Any>,
    var onItemClicked: (item: Any) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val language = 1
        const val units = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            language -> SettingsHolder(
                ItemSettingsElementBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                ),
            ) { onItemClicked(it) }
            else -> {
                SettingsHolder(
                    ItemSettingsElementBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    ),
                ) { onItemClicked(it) }
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ApplicationLanguage -> language
        else -> units
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SettingsHolder -> holder.bind(items[position])
            else -> units
        }
    }

    override fun getItemCount() = items.size
}