package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Docs Z", appName)
  }

  @Test
  fun `scale calibration accurately converts pixels to architectural feet`() {
    val scale = com.example.model.ScaleCalibration(
      pixelDistance = 100f,
      realDistance = 10f,
      unit = "ft"
    )
    val formatted = scale.format(200f)
    assertEquals("20' - 0\"", formatted)
  }
}
