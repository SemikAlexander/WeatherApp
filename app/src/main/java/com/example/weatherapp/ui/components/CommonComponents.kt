package com.example.weatherapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.weatherapp.R
import com.example.weatherapp.ui.theme.WeatherAppTheme

@Composable
fun CircleIconButton(
    iconResId: Int,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 44,
    tint: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.surface,
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
            modifier = Modifier.size((size * 0.55f).dp),
        )
    }
}

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
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            SpacerWidth(8)
        }
        Icon(
            painter = painterResource(R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(19.dp),
        )
    }
}

@Composable
fun SwitchSettingRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Switch,
            ) { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
fun InfoTile(
    iconResId: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
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
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
    }
}

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
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        if (isSelected) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = stringResource(R.string.save_button),
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

// region Previews

@Preview(name = "Light", showBackground = true)
@Composable
private fun CircleIconButtonPreview() {
    WeatherAppTheme(darkTheme = false) {
        CircleIconButton(
            iconResId = R.drawable.ic_menu,
            contentDescription = "Settings",
            onClick = {},
        )
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun CircleIconButtonDarkPreview() {
    WeatherAppTheme(darkTheme = true) {
        CircleIconButton(
            iconResId = R.drawable.ic_menu,
            contentDescription = "Settings",
            onClick = {},
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun SettingsRowPreview() {
    WeatherAppTheme(darkTheme = false) {
        SettingsRow(title = "Language", value = "English, EN", onClick = {})
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun SettingsRowDarkPreview() {
    WeatherAppTheme(darkTheme = true) {
        SettingsRow(title = "Language", value = "English, EN", onClick = {})
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun SwitchSettingRowPreview() {
    WeatherAppTheme(darkTheme = false) {
        SwitchSettingRow(
            title = stringResource(R.string.dark_mode),
            checked = true,
            onCheckedChange = {},
        )
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun SwitchSettingRowDarkPreview() {
    WeatherAppTheme(darkTheme = true) {
        SwitchSettingRow(
            title = stringResource(R.string.dark_mode),
            checked = false,
            onCheckedChange = {},
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun InfoTilePreview() {
    WeatherAppTheme(darkTheme = false) {
        InfoTile(
            iconResId = R.drawable.ic_sunrise,
            label = stringResource(R.string.sunrise),
            value = "06:40 AM",
            modifier = Modifier.fillMaxWidth(0.33f),
        )
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun InfoTileDarkPreview() {
    WeatherAppTheme(darkTheme = true) {
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
    WeatherAppTheme(darkTheme = false) {
        Column {
            SelectableItemRow(name = "English, EN", isSelected = true, onClick = {})
            SelectableItemRow(name = "Russian, RU", isSelected = false, onClick = {})
        }
    }
}

// endregion