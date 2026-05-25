package com.example.flingapp.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.flingapp.presentation.viewmodel.FlingViewModel

/**
 * Composable-Funktion für den Hauptbildschirm der FlingApp.
 *
 * @param viewModel ViewModel für den Zugriff auf den Spielzustand und Aktionen
 * @param modifier Optionaler Modifier für Layout-Anpassungen
 */
@Composable
fun FlingScreen(
    viewModel: FlingViewModel,
    modifier: Modifier = Modifier
) {
    // reactively observe changes from viewModel
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Fling Ding",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(text = "Highscore", style = MaterialTheme.typography.labelMedium)
        InfoCard(
            label = "Aktueller Rekord",
            value = state.highscore.toInt(),
            modifier = Modifier.testTag("highscore_card"),
            valueTag = "highscore_value"
        )

        Text(text = "Letzter Versuch", style = MaterialTheme.typography.labelMedium)
        InfoCard(
            label = "Letzte Geschwindigkeit",
            value = state.lastAttempt.toInt(),
            modifier = Modifier.testTag("last_attempt_card"),
            valueTag = "last_attempt_value"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // progress bar shows highscore relative to max value (5000)
        LinearProgressIndicator(
            progress = { (state.highscore / 5000f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .testTag("progress_bar")
        )
        Text(text = "Max: 5000", style = MaterialTheme.typography.bodySmall)

        // gravity toggle as checkbox
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Checkbox(
                checked = state.isGravityActive,
                onCheckedChange = { viewModel.toggleGravity(it) },
                modifier = Modifier.testTag("gravity_checkbox")
            )
            Text(text = "Schwerkraft aktiv")
        }

        // reset button
        Button(
            onClick = { viewModel.resetHighscore() },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("restart_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Neustart (Setzt Highscore zurück)")
        }
    }
}

/**
 * Wiederverwendbare Kartenkomponente für die Anzeige von Spielinformationen.
 *
 * @param label Beschriftung der Information 
 * @param value Anzuzeigender Wert 
 * @param modifier Optionaler Modifier für Layout-Anpassungen
 * @param valueTag Optionaler Test-Tag für den Wert-Text
 */
@Composable
fun InfoCard(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
    valueTag: String = ""
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = (if (valueTag.isNotEmpty()) Modifier.testTag(valueTag) else Modifier)
                    .fillMaxWidth()
            )
        }
    }
}
