package com.anyline.examples.viewConfigEditor

import android.content.Context
import android.webkit.ConsoleMessage
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.webkit.WebResourceErrorCompat
import com.anyline.examples.viewConfigEditor.WebContentFragmentCallback.UrlData
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

enum class ViewConfigEditorDefinition(val definition: String) {
    ScanViewConfig(""),
    CameraConfig("#/definitions/cameraConfig"),
    FlashConfig("#/definitions/flashConfig"),
    ViewPluginConfig("#/definitions/viewPluginConfig"),
    ViewPluginCompositeConfig("#/definitions/viewPluginCompositeConfig"),
    PluginConfig("#/definitions/viewPluginConfig/pluginConfig"),
    CutoutConfig("#/definitions/viewPluginConfig/definitions/cutoutConfig"),
    ScanFeedbackConfig("#/definitions/viewPluginConfig/definitions/scanFeedbackConfig"),
    UIFeedbackConfig("#/definitions/viewPluginConfig/uiFeedbackConfig")
}

class ViewConfigEditorFragment(viewConfigEditorDefinition: ViewConfigEditorDefinition,
                                jsonContent: JSONObject?,
                                val onJsonApply: ((JSONObject) -> Unit)
    ): WebContentFragment() {
    private val editorHomeUrl = "viewConfigEditor/json-editor-css.html"

    private val jsonSchemaRemoteUrl = "https://documentation.anyline.com/android-sdk-component/latest/_attachments/json-schemas/sdk_config.schema.json"

    init {
        webContentCallback = object : WebContentFragmentCallback {
            override fun getHomeUrl(context: Context): String? = null

            override fun getHomeUrlData(context: Context): UrlData? {
                val schemaContent = "{\"\$ref\": \"${jsonSchemaRemoteUrl + viewConfigEditorDefinition.definition}\"}"
                var htmlContent = context.assets.open(editorHomeUrl).bufferedReader().use {
                    it.readText()
                }
                htmlContent = htmlContent.replace("schema: {}", "schema: $schemaContent")
                htmlContent = htmlContent.replace("starting_value = {}", "starting_value = ${jsonContent.toString()}")
                return UrlData(data = htmlContent, baseUrl = "file:///android_asset/viewConfigEditor/")
            }

            override fun isSwipeRefreshEnabled() = false
            override fun isFileChooserEnabled() = false
            override fun shouldOpenUrlInExternalBrowser(view: WebView, url: String) = false

            override fun onPageFinished(view: WebView, url: String) {
                Timber.tag("ViewConfigEditorFragment.onPageFinished")
                    .d(url)
            }

            override fun onReceivedError(
                webView: WebView,
                webResourceRequest: WebResourceRequest,
                webResourceErrorCompat: WebResourceErrorCompat
            ): Boolean {
                return true
            }

            override fun onReceivedTitle(title: String) {
                //nothing to do with tile here
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage) {
                val consoleMessageString = consoleMessage.message().toString()
                try {
                    val consoleMessageJson = JSONObject(consoleMessageString)
                    val readyState = consoleMessageJson.optBoolean("readyState", false)
                    if (readyState) {
                        this@ViewConfigEditorFragment.onEditorFinishedLoading()
                    }
                    consoleMessageJson.optJSONObject("content")?.let { updatedJson ->
                        onJsonApply.invoke(updatedJson)
                    }
                }
                catch (e: JSONException) {
                    //ignore invalid console output
                }
            }
        }
    }

    fun onEditorFinishedLoading() {
        super.switchLoadingState(false, null)
    }

    override fun switchLoadingState(loadingState: Boolean, error: WebResourceErrorCompat?) {
        if (!loadingState && error == null) {
            //ignore finished connection state changes, only apply when editor finished loading
            return
        }
        super.switchLoadingState(loadingState, error)
    }
}