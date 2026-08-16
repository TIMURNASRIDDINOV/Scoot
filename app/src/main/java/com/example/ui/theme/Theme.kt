package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Bold Typography Light Color Scheme (Primary design theme)
private val BoldColorScheme =
    lightColorScheme(
        primary = BoldAccentVolt,
        onPrimary = BoldDarkInk,
        primaryContainer = BoldSageSoft,
        onPrimaryContainer = BoldDarkInk,
        secondary = BoldSageSoft,
        onSecondary = BoldDarkInk,
        secondaryContainer = BoldStoneContainer,
        onSecondaryContainer = BoldDarkInk,
        tertiary = ScootYellow,
        onTertiary = BoldDarkInk,
        background = BoldCreamBg,
        onBackground = BoldDarkInk,
        surface = BoldWhiteCard,
        onSurface = BoldDarkInk,
        surfaceVariant = BoldStoneContainer,
        onSurfaceVariant = BoldTextMuted,
        outline = BoldDarkInk,
        outlineVariant = BoldStoneContainer,
        error = ScootRed,
        onError = Color.White
    )

@Composable
fun ScootTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
  val colorScheme = BoldColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = colorScheme.background.toArgb()
        window.navigationBarColor = colorScheme.background.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
      }
    }
  }

  MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
  )
}


