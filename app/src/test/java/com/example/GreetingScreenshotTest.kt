package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.RegistryDatabase
import com.example.data.RegistryRepository
import com.example.ui.RegistryMainScreen
import com.example.ui.RegistryViewModel
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
    val context = ApplicationProvider.getApplicationContext<Context>()
    
    // In-memory db setup to cleanly support automated visual screenshot generation
    val db = Room.inMemoryDatabaseBuilder(context, RegistryDatabase::class.java).build()
    val repository = RegistryRepository(db.registryDao())
    val viewModel = RegistryViewModel(repository)

    composeTestRule.setContent { 
      MyApplicationTheme { 
        RegistryMainScreen(viewModel = viewModel) 
      } 
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    
    db.close()
  }
}
