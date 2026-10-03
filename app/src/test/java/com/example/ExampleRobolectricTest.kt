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
    assertEquals("BD Bus Simulator", appName)
  }

  @Test
  fun `verify bus repository contains starter bus and routes`() {
    val hinoBus = com.example.data.BusRepository.getBusById("bus_hino_1j")
    assertEquals("Hino 1J Classic", hinoBus.name)
    assertEquals(true, com.example.data.BusRepository.allRoutes.isNotEmpty())
  }
}
