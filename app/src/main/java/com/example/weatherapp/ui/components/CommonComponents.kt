package com.example.weatherapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.example.weatherapp.R
import com.example.weatherapp.ui.theme.*

/**
 * Переиспользуемые примитивы UI.
 *
 * Правило проекта: повторяющиеся элементы выносятся в базовые Composable-функции
 * отсюда; конкретные экраны собирают из этих кирпичей в своих объектах-неймспейсах
 * (см. MainUI / SettingsUI). Каждый элемент имеет @Preview — вёрстку можно править
 * без запуска приложения.
 */

/** Круглая кнопка-иконка (настройки на Home, «назад» на Settings). */
@Composable
fun CircleIconButton(
    iconResId: Int,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 44,
    tint: Color = Color.White,
    containerColor: Color = c006485,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(containerColor, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(DpSize(width = size.dp, height = size.dp)),
        )
    }
}

/** Пункт списка настроек: заголовок + текущее значение + стрелка. */
@Composable
fun SettingsRow(
    title: String,
    value: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(c006485)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            modifier = Modifier.weight(1f),
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
            )
            Spacer(Modifier.size(8.dp))
        }
        Icon(
            painter = painterResource(R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(19.dp),
        )
    }
}

/** Инфо-плитка погоды: иконка + подпись + значение (Sunrise/Sunset/Wind/Pressure/Humidity). */
@Composable
fun InfoTile(
    iconResId: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(InfoTabBackground)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(iconResId),
            contentDescription = null,
            modifier = Modifier.size(25.dp),
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

/** Строка выбора в диалоге настроек: название + галочка у активного варианта. */
@Composable
fun SelectableItemRow(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier.weight(1f),
        )
        if (isSelected) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = stringResource(R.string.save_button),
                tint = c006485,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

// ---------- Previews ----------

@Preview(showBackground = true, backgroundColor = 0xFF0099CC)
@Composable
private fun CircleIconButtonPreview() {
    WeatherAppTheme {
        CircleIconButton(
            iconResId = R.drawable.synchronize,
            contentDescription = "Settings",
            onClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF006485)
@Composable
private fun SettingsRowPreview() {
    WeatherAppTheme {
        SettingsRow(title = "Language", value = "English, EN", onClick = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0099CC)
@Composable
private fun InfoTilePreview() {
    WeatherAppTheme {
        InfoTile(
            iconResId = R.drawable.ic_sunrise,
            label = stringResource(R.string.sunrise),
            value = "06:40 AM",
            modifier = Modifier.fillMaxWidth(0.33f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectableItemRowPreview() {
    WeatherAppTheme {
        Column {
            SelectableItemRow(name = "English, EN", isSelected = true, onClick = {})
            SelectableItemRow(name = "Russian, RU", isSelected = false, onClick = {})
        }
    }
}
