package com.example.weatherapp.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SpacerHeight(num: Int) = Spacer(Modifier.height(num.dp))

@Composable
fun SpacerHeight(num: Float) = Spacer(Modifier.height(num.dp))

@Composable
fun SpacerHeight(min: Int, max: Int) = Spacer(Modifier.heightIn(min = min.dp, max = max.dp))

@Composable
fun SpacerWidth(num: Int) = Spacer(Modifier.width(num.dp))

@Composable
fun ColumnScope.SpacerWeight() = Spacer(Modifier.weight(1f))

@Composable
fun RowScope.SpacerWeight(weight: Float = 1f) = Spacer(Modifier.weight(weight))