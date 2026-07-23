package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = SleekDarkPrimary,
    secondary = SleekDarkSecondary,
    tertiary = SleekDarkTertiary,
    background = SleekDarkBackground,
    surface = SleekDarkSurface,
    onPrimary = SleekDarkBackground,
    onSecondary = SleekDarkBackground,
    onTertiary = SleekDarkBackground,
    onBackground = SleekDarkOnBackground,
    onSurface = SleekDarkOnSurface,
    surfaceVariant = SleekDarkSurfaceVariant,
    onSurfaceVariant = SleekDarkOnSurfaceVariant,
    outline = SleekDarkOutline
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SleekLightPrimary,
    secondary = SleekLightSecondary,
    tertiary = SleekLightTertiary,
    background = SleekLightBackground,
    surface = SleekLightSurface,
    onPrimary = SleekLightSurface,
    onSecondary = SleekLightSurface,
    onTertiary = SleekLightSurface,
    onBackground = SleekLightOnBackground,
    onSurface = SleekLightOnSurface,
    surfaceVariant = SleekLightSurfaceVariant,
    onSurfaceVariant = SleekLightOnSurfaceVariant,
    outline = SleekLightOutline
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  accentColor: String = "default",
  content: @Composable () -> Unit,
) {
  val useDynamicColor = accentColor.lowercase() == "wallpaper" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
  val colorScheme =
    when {
      useDynamicColor -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      else -> {
        val primaryColor = when (accentColor.lowercase()) {
          "green" -> androidx.compose.ui.graphics.Color(0xFF10B981)
          "orange" -> androidx.compose.ui.graphics.Color(0xFFF97316)
          "purple" -> androidx.compose.ui.graphics.Color(0xFF8B5CF6)
          "rose" -> androidx.compose.ui.graphics.Color(0xFFF43F5E)
          else -> if (darkTheme) SleekDarkPrimary else SleekLightPrimary
        }
        val secondaryColor = when (accentColor.lowercase()) {
          "green" -> androidx.compose.ui.graphics.Color(0xFF059669)
          "orange" -> androidx.compose.ui.graphics.Color(0xFFD97706)
          "purple" -> androidx.compose.ui.graphics.Color(0xFF7C3AED)
          "rose" -> androidx.compose.ui.graphics.Color(0xFFE11D48)
          else -> if (darkTheme) SleekDarkSecondary else SleekLightSecondary
        }

        if (darkTheme) {
          DarkColorScheme.copy(
            primary = primaryColor,
            secondary = secondaryColor,
            tertiary = if (accentColor.lowercase() == "default") SleekDarkTertiary else secondaryColor
          )
        } else {
          LightColorScheme.copy(
            primary = primaryColor,
            secondary = secondaryColor,
            tertiary = if (accentColor.lowercase() == "default") SleekLightTertiary else secondaryColor
          )
        }
      }
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
