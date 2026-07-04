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
    assertEquals("Apna Hisaab", appName)
  }

  @Test
  fun `verify saving monthly budget preferences`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sharedPrefs = context.getSharedPreferences("apna_hisaab_prefs", Context.MODE_PRIVATE)
    
    sharedPrefs.edit().putFloat("monthly_budget", 15000f).commit()
    
    val budget = sharedPrefs.getFloat("monthly_budget", 0f)
    assertEquals(15000f, budget, 0.01f)
  }
}
