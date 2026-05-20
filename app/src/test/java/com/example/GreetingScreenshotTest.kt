package com.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.data.QuitProfile
import com.example.ui.CountdownDashboard
import com.example.ui.HeroBanner
import com.example.ui.TimeRemaining
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          HeroBanner(
            profile = QuitProfile(
              id = 0,
              quitTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 5,
              userName = "পরীক্ষামূলক যোদ্ধা",
              cravingsResisted = 3,
              breathsCompleted = 2
            )
          )
          Spacer(modifier = Modifier.height(16.dp))
          CountdownDashboard(
            timePassed = TimeRemaining(days = 12, hours = 5, minutes = 32, seconds = 19),
            profile = QuitProfile(
              id = 0,
              quitTimestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 5,
              userName = "পরীক্ষামূলক যোদ্ধা",
              cravingsResisted = 3,
              breathsCompleted = 2
            ),
            onCustomizeClick = {},
            onResistCraving = {}
          )
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
