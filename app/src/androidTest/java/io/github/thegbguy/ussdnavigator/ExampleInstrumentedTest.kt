/*
 * *
 *  * Created by Chiranjeevi Pandey on 26/3/22, 1:07 pm
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 26/3/22, 1:07 pm
 *
 */

package io.github.thegbguy.ussdnavigator

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("io.github.thegbguy.ussdnavigator", appContext.packageName)
    }
}