package com.crewup.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary        = CrewUpBlack,
    onPrimary      = CrewUpWhite,
    secondary      = CrewUpGrayDark,
    onSecondary    = CrewUpWhite,
    background     = CrewUpWhite,
    surface        = CrewUpGray,
    onBackground   = CrewUpBlack,
    onSurface      = CrewUpBlack,
    outline        = CrewUpDivider
)

@Composable
fun CrewUpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography  = Typography,
        content     = content
    )
}