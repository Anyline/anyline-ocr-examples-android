package com.anyline.examples.viewConfigEditor

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.Button
import android.widget.LinearLayout
import androidx.webkit.WebResourceErrorCompat
import at.nineyards.anyline.BuildConfig.VERSION_NAME_ANYLINE_SDK
import com.anyline.examples.R
import com.anyline.examples.viewConfigEditor.WebContentFragmentCallback.UrlData
import org.json.JSONException
import org.json.JSONObject

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

class ViewConfigEditorFragment(
    private val viewConfigEditorDefinition: ViewConfigEditorDefinition,
    private val jsonContent: JSONObject?,
    val onJsonApply: ((JSONObject) -> Unit)
) : WebContentFragment() {

    private val jsonSchemaRemoteUrl =
       "https://documentation.anyline.com/android-sdk-component/${VERSION_NAME_ANYLINE_SDK}/mobile-sdk-common/_attachments/json-schemas/sdk_config.schema.json"


    private var editorWebView: WebView? = null

    private lateinit var buttonApply: Button
    private lateinit var buttonRestore: Button

    init {
        webContentCallback = object : WebContentFragmentCallback {
            override fun getHomeUrl(context: Context): String? = null

            // Page loading is handled manually in onViewCreated after JS interfaces are registered.
            override fun getHomeUrlData(context: Context): UrlData? = null

            override fun isSwipeRefreshEnabled() = false
            override fun isFileChooserEnabled() = false
            override fun shouldOpenUrlInExternalBrowser(view: WebView, url: String) = false

            override fun onPageFinished(view: WebView, url: String) {
                val schemaRef = jsonSchemaRemoteUrl + viewConfigEditorDefinition.definition
                val schemaJson = "{\"\$ref\":\"$schemaRef\"}"
                val valueJson = jsonContent?.toString() ?: "{}"
                view.evaluateJavascript(
                    "loadData(${JSONObject.quote(schemaJson)}, ${JSONObject.quote(valueJson)}, true, false)",
                    null
                )
            }

            override fun onReceivedError(
                webView: WebView,
                webResourceRequest: WebResourceRequest,
                webResourceErrorCompat: WebResourceErrorCompat
            ): Boolean = true

            override fun onReceivedTitle(title: String) {}

            override fun onConsoleMessage(consoleMessage: ConsoleMessage) {}
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val webContent = super.onCreateView(inflater, container, savedInstanceState)

        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
        root.addView(webContent, LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f))

        val dp8 = (8 * resources.displayMetrics.density).toInt()
        val dp4 = dp8 / 2

        buttonRestore = Button(requireContext()).apply {
            text = "Restore to Default"
            isAllCaps = false
        }
        buttonApply = Button(requireContext()).apply {
            text = "Apply Changes"
            isAllCaps = false
        }

        val buttonBar = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp8, dp4, dp8, dp4)
            setBackgroundColor(Color.WHITE)
            addView(
                buttonRestore,
                LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f).also { it.marginEnd = dp4 }
            )
            addView(
                buttonApply,
                LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f).also { it.marginStart = dp4 }
            )
        }
        root.addView(buttonBar, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Register JS interfaces before loading the page so the shared HTML detects them.
        editorWebView = view.findViewById<WebView>(R.id.webview)?.also {
            it.addJavascriptInterface(ReadyJsBridge(), "ReadyChannel")
            it.addJavascriptInterface(ErrorJsBridge(), "ErrorChannel")
        }

        // Load the shared html with inlined assets now that interfaces are registered.
        loadDataWithBaseURL(buildUrlData(view.context))

        buttonApply.setOnClickListener {
            // Read the current editor value directly — bypasses the validation gate in the
            // change handler so edits are always applied regardless of oneOf warnings.
            editorWebView?.evaluateJavascript(
                "(function(){ return typeof _editor !== 'undefined' ? _editor.getValue() : null; })()"
            ) { result ->
                if (result != null && result != "null") {
                    try { onJsonApply(JSONObject(result)) } catch (_: JSONException) {}
                }
            }
        }

        buttonRestore.setOnClickListener {
            val startVal = JSONObject.quote(jsonContent?.toString() ?: "{}")
            editorWebView?.evaluateJavascript(
                "if(typeof _editor!=='undefined') _editor.setValue(JSON.parse($startVal))",
                null
            )
        }
    }

    private fun buildUrlData(context: Context): UrlData {
        val assets = context.assets
        var html = assets.open("viewConfigEditor/json-editor.html").bufferedReader().use { it.readText() }
        html = html.replace("{{JSONEDITOR_JS}}", assets.open("viewConfigEditor/jsoneditor.js").bufferedReader().use { it.readText() })
        html = html.replace("{{SPECTRE_CSS}}", assets.open("viewConfigEditor/spectre.min.css").bufferedReader().use { it.readText() })
        html = html.replace("{{SPECTRE_EXP_CSS}}", assets.open("viewConfigEditor/spectre-exp.min.css").bufferedReader().use { it.readText() })
        html = html.replace("{{SPECTRE_ICONS_CSS}}", assets.open("viewConfigEditor/spectre-icons.min.css").bufferedReader().use { it.readText() })
        return UrlData(data = html, baseUrl = "file:///android_asset/viewConfigEditor/")
    }

    fun onEditorFinishedLoading() {
        super.switchLoadingState(false, null)
    }

    override fun switchLoadingState(loadingState: Boolean, error: WebResourceErrorCompat?) {
        if (!loadingState && error == null) {
            // Ignore page-load completion — wait for ReadyChannel signal from the editor.
            return
        }
        super.switchLoadingState(loadingState, error)
    }

    inner class ReadyJsBridge {
        @JavascriptInterface
        fun postMessage(height: String) {
            Handler(Looper.getMainLooper()).post { onEditorFinishedLoading() }
        }
    }

    inner class ErrorJsBridge {
        @JavascriptInterface
        fun postMessage(message: String) {
            switchErrorState(true, message)
        }
    }
}