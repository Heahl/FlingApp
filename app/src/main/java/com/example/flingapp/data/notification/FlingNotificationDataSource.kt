package com.example.flingapp.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Android-spezifische Implementierung für Benachrichtigungen.
 *
 * Verwaltet die Erstellung und den Versand von Benachrichtigungen für die FlingApp, insbesondere für Highscore- 
 * Ergebnisse.
 *
 * ### Funktionen: 
 * - Erstellt den notwendigen Notification Channel (erforderlich ab Android 8.0)
 * - Prüft Benachrichtigugs-Berechtigungen (erforderlich ab Android 13)
 * - Sendet Highscore-Benachrichtigungen mit Spielinformationen
 * - Verwendet korrekte Priorisierung für Benachrichtigungen
 *
 * @param context Der Android-Kontext für Systemdienste
 */
class FlingNotificationDataSource(private val context: Context) {

    /**
     * ID für den Notification Channel.
     *
     * Wird verwendet, um den Channel eindeutig zu identifizieren. Muss für die gesamte Anwendung eindeutig sein.
     * Änderungen an diesem Wert führen zu einem neuen Channel.
     */
    private val channelId = "fling_highscore_channel"

    /**
     * Eindeutige ID für die Benachrichtigung.
     *
     * Wird verwendet, um die Benachrichtigung zu identifizieren und bei Bedarf zu aktualisieren oder zu ersetzen.
     * Für Highscore-Benachrichtigungen wird dieselbe ID verwendet, um nur die neueste Benachrichtigung anzuzeigen.
     */
    private val notificationId = 1

    init {
        // create notification channel during initialization
        createNotificationChannel()
    }

    /**
     * Erstellt den Notification Channel für Highscore-Benachrichtigungen.
     *
     * Ab Android 8.0 (API 26) sind Notification Channels erforderlich, um Benachrichtigungen anzeigen zu können.
     * Dieser Channel wird einmalig erstell und bleibt für die gesamte Lebensdauer der App bestehen.
     */
    private fun createNotificationChannel() {
        val name = "Fling Highscore"
        val descriptionText = "Notifications for breaking the highscore"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // create notification channel - no op if channel already exists
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Sendet eine Benachrichtigungen über einen neuen Highscore.
     *
     * Erstellt und zeigt eine Benachrichtigung an, wenn der Benutzer einen neuen Highscore erreicht hat.
     * Prüft vor Senden, ob die notwendigen Berechtigungen erteilt wurden.
     *
     * @param score Erreichte Geschwindigkeit des Fling-Vorgangs
     */
    fun sendHighscoreNotification(score: Float) {
        // cheack for notification permission
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // build notification
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.star_on)
            .setContentTitle("Neuer Highscore!")
            .setContentText("Du hast einen Fling mit einer Geschwindigkeit von ${score.toInt()} erreicht!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // dismisses when user taps notification

        // use NotificationManagerCompat for backward compatibility
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}
