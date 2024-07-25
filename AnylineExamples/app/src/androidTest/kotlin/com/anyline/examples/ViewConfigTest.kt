package com.anyline.examples

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import io.anyline2.sdk.ScanViewConfigHolder
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class ViewConfigTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var context: Context
    @Before
    fun setUp() {
        context = instrumentation.targetContext.applicationContext
    }

    @Test
    fun validateViewConfigs() {
        validateAllViewConfigsInAssetsSubFolder("viewConfigs")
    }

    private fun validateAllViewConfigsInAssetsSubFolder(assetsSubFolder: String) {
        val viewConfigFolderFiles = context.assets.list(assetsSubFolder)
        viewConfigFolderFiles?.forEach { fileName ->
            val fileContent = context.assets.open("$assetsSubFolder/$fileName").bufferedReader().use {
                it.readText()
            }
            val jsonObject = JSONObject(fileContent)

            val validationResult = ScanViewConfigHolder.validateJsonObject(context, jsonObject)
            when (validationResult) {
                is ScanViewConfigHolder.ScanViewJsonValidationResult.ValidationSucceeded ->
                    assert(true) {
                        "file $fileName contains a valid ScanViewConfig"
                    }
                is ScanViewConfigHolder.ScanViewJsonValidationResult.ValidationFailed ->
                    assert(false) {
                        "file $fileName does not contain a valid ScanViewConfig: ${validationResult.exception.message}"
                    }
            }
        }
    }
}