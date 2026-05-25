package com.example.flingapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import android.Manifest
import com.example.flingapp.presentation.ui.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlingAppInstrumentedTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @Test
    fun initialState_isCorrect() {
        // Verify Highscore is 0
        composeTestRule.onNodeWithTag("highscore_value").assertTextEquals("0")

        // Verify Last Attempt is 0
        composeTestRule.onNodeWithTag("last_attempt_value").assertTextEquals("0")

        // Verify Gravity Checkbox is unchecked
        composeTestRule.onNodeWithTag("gravity_checkbox").assertIsOff()

        // Verify Progress Bar is at 0
        // (Note: progress is 0f, but we can't easily check the numerical progress value via tags without custom semantics)
        composeTestRule.onNodeWithTag("progress_bar").assertExists()
    }

    @Test
    fun restartButton_resetsHighscore() {
        // Set a highscore via the ViewModel
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.viewModel.updateAttempt(3000f) {}
        }

        // Verify highscore is updated
        composeTestRule.onNodeWithTag("highscore_value").assertTextEquals("3000")

        // Click Restart
        composeTestRule.onNodeWithTag("restart_button").performClick()

        // Verify highscore is 0 again
        composeTestRule.onNodeWithTag("highscore_value").assertTextEquals("0")
    }

    @Test
    fun gravityFeature_decreasesScore() {
        // Set a highscore via the ViewModel
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.viewModel.updateAttempt(1000f) {}
        }

        // Activate Gravity
        composeTestRule.onNodeWithTag("gravity_checkbox").performClick()
        composeTestRule.onNodeWithTag("gravity_checkbox").assertIsOn()

        // Wait for highscore to decrease below 950
        composeTestRule.waitUntil(5000) {
            val text = composeTestRule.onNodeWithTag("highscore_value")
                .fetchSemanticsNode().config.getOrNull(SemanticsProperties.Text)
                ?.firstOrNull()?.text ?: ""
            val score = text.toIntOrNull() ?: 1001
            score <= 950
        }

        // Wait for highscore to decrease further below 850
        composeTestRule.waitUntil(5000) {
            val text = composeTestRule.onNodeWithTag("highscore_value")
                .fetchSemanticsNode().config.getOrNull(SemanticsProperties.Text)
                ?.firstOrNull()?.text ?: ""
            val score = text.toIntOrNull() ?: 1001
            score <= 850
        }

        // Get current value to verify it stops
        val valueWhenStopping =
            composeTestRule.onNodeWithTag("highscore_value").fetchSemanticsNode().config.getOrNull(
                SemanticsProperties.Text
            )?.firstOrNull()?.text ?: ""

        // Deactivate Gravity
        composeTestRule.onNodeWithTag("gravity_checkbox").performClick()
        composeTestRule.onNodeWithTag("gravity_checkbox").assertIsOff()

        // Verify it stopped (wait a bit and check if it's still the same)
        Thread.sleep(1000)
        composeTestRule.onNodeWithTag("highscore_value").assertTextEquals(valueWhenStopping)
    }
}
