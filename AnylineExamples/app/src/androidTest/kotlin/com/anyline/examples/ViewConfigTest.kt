package com.anyline.examples

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import io.anyline2.sdk.ScanViewConfigHolder
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException

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
        listAssetFiles(assetsSubFolder) { isDir, path ->
            if (!isDir) {
                val fileContent = context.assets.open(path).bufferedReader().use {
                    it.readText()
                }
                val jsonObject = JSONObject(fileContent)

                val validationResult = ScanViewConfigHolder.validateJsonObject(context, jsonObject)
                when (validationResult) {
                    is ScanViewConfigHolder.ScanViewJsonValidationResult.ValidationSucceeded ->
                        assert(true) {
                            "file $path contains a valid ScanViewConfig"
                        }
                    is ScanViewConfigHolder.ScanViewJsonValidationResult.ValidationFailed ->
                        assert(false) {
                            "file $path does not contain a valid ScanViewConfig: ${validationResult.exception.message}"
                        }
                }
            }
        }
    }

    private fun listAssetFiles(path: String, block: (isDir: Boolean, path: String) -> Unit): Boolean {
        try {
            val list = context.assets.list(path)
            list?.also { files->
                if (files.isNotEmpty()) {
                    for (file in files) {
                        val relativePath = if (path.isEmpty()) file else "$path${File.separatorChar}$file"
                        if (!listAssetFiles(relativePath, block))
                            block.invoke(false, relativePath) else block.invoke(true, relativePath)
                    }
                } else return false
            }
        } catch (e: IOException) {
            return false
        }
        return true
    }
}