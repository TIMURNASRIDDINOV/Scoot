package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.AppMode
import com.example.ui.ScootViewModel
import com.example.ui.fleet.FleetOpsScreen
import com.example.ui.rider.RiderScreen
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.ScootGreen
import com.example.ui.theme.ScootRed
import com.example.ui.theme.ScootTheme

class MainActivity : ComponentActivity() {

  private val viewModel: ScootViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      ScootTheme(darkTheme = true) {
        val appMode by viewModel.appMode.collectAsState()
        val notification by viewModel.notification.collectAsState()

        Box(modifier = Modifier.fillMaxSize()) {
          AnimatedContent(
            targetState = appMode,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "AppModeTransition"
          ) { mode ->
            when (mode) {
              AppMode.RIDER -> RiderScreen(viewModel = viewModel)
              AppMode.FLEET_BACKOFFICE -> FleetOpsScreen(viewModel = viewModel)
            }
          }

          // Top floating in-app notification banner
          notification?.let { notif ->
            LaunchedEffect(notif.id) {
              kotlinx.coroutines.delay(3500)
              viewModel.clearNotification()
            }

            Surface(
              color = DarkSurface.copy(alpha = 0.96f),
              border = BorderStroke(1.dp, if (notif.isError) ScootRed else ScootGreen),
              shape = RoundedCornerShape(14.dp),
              shadowElevation = 8.dp,
              modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 80.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .testTag("app_notification_banner")
            ) {
              Text(
                text = notif.message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (notif.isError) ScootRed else Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
              )
            }
          }
        }
      }
    }
  }
}

