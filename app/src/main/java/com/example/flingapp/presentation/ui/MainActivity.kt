package com.example.flingapp.presentation.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flingapp.data.repository.FlingRepositoryImpl
import com.example.flingapp.domain.usecase.*
import com.example.flingapp.presentation.ui.theme.FlingAppTheme
import com.example.flingapp.presentation.viewmodel.FlingViewModel

/**
 * Haupt-Aktivität der Fling-Anwendung.
 */
class MainActivity : ComponentActivity() {

    /**
     * ViewModel für die FlingView 
     *
     * Wird mit viewModels delegator initialisiert, um den lifecycle zu verwalten.
     * Überlebt Konfigurationsänderungen und stellt den Spielzustand für das UI bereit.
     */
    val viewModel: FlingViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(FlingViewModel::class.java)) {
                    val repository = FlingRepositoryImpl(applicationContext)
                    return FlingViewModel(
                        GetFlingStateUseCase(repository),
                        ToggleGravityUseCase(repository),
                        ResetHighscoreUseCase(repository),
                        StartSensingUseCase(repository),
                        StopSensingUseCase(repository),
                        ProcessFlingUseCase(repository)
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    /**
     * Launcher für die Berechtigungsanfrage für Benachrichtigungen. 
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
          // make user understand what won't work if he denies permission
            Toast.makeText(this, "Berechtigung für Highscore-Infos fehlt.", Toast.LENGTH_LONG)
                .show()
        }
    }

    /**
     * Wird beim ersten Erstellen der Activity aufgerufen.
     *
     * Initialisiert das UI und setzt die notwendigen Konfigurationen:
     * 1. Aktiviert Edge-to-Edge-Display 
     * 2. Prüft und fordert Benachrichtigung-Berechtigungen an
     * 3. Setzt den Compose-Inhalt mit dem FlingAppTheme
     * 4. Konfiguriert die Scaffold-Struktur
     *
     * @param savedInstanceState Gespeicherter Zustand bei Konfigurationsänderungen
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // extend content to edges of screen
        enableEdgeToEdge()

        // check and request notification permission early
        checkNotificationPermission()

        // set compose ui
        setContent {
            FlingAppTheme {
              // provide mui structure with top app bar
                Scaffold { innerPadding ->
                    FlingScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    /**
     * Prüft und fordert die Benachrichtigungs-Berechtigung an.
     *
     * Ab Android 13 (API 33) ist die POST_NOTIFICATIONS Berechtigung erforderlich, um Benachrichtigungen 
     * anzuzeigen. 
     */
    private fun checkNotificationPermission() {
      // check for permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
          // request permission
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /**
     * Wird aufgerufen, wenn die Activity wieder in den Vordergrund kommt.
     *
     * Startet die Sensor-Überwachung, um Fling-Bewegungen zu erkennen. 
     * Wird beim ersten Start und nach Rückkehr aus dem Hintergrund aufgerufen.
     */
    override fun onResume() {
        super.onResume()
        // start sensor monitoring when activity becomes visible
        viewModel.startSensing()
    }

    /**
     * Wird aufgerufen, wenn die Activity in den Hintergrund geht.
     *
     * Stoppt die Sensor-Überwachung, um Akku zu sparen. 
     * Wird beim Wechsel zu einer anderen Activity oder beim Schließen der App aufgerufen.
     */
    override fun onPause() {
        super.onPause()
        // stop sensor monitoring
        viewModel.stopSensing()
    }
}
