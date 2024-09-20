package com.anyline.examples.viewConfigEditor

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.webkit.WebResourceErrorCompat
import androidx.webkit.WebViewClientCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.WebViewFeature.WEB_RESOURCE_ERROR_GET_DESCRIPTION
import com.anyline.examples.R
import com.anyline.examples.databinding.FragmentWebcontentBinding

open class WebContentFragment : Fragment(), WebContentFragmentAction {

    private lateinit var binding: FragmentWebcontentBinding

    protected var webContentCallback: WebContentFragmentCallback? = null

    private val ignoredErrorCodes = listOf(WebViewClient.ERROR_UNKNOWN)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentWebcontentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWebView()
        webContentCallback?.getHomeUrl(view.context)?.let {
            loadUrl(it)
        } ?: run {
            webContentCallback?.getHomeUrlData(view.context)?.let {
                loadDataWithBaseURL(it)
            }
        }
    }

    override fun onDetach() {
        binding.webview.apply {
            webChromeClient = null;
            clearHistory();
            destroy();
        }
        webContentCallback = null
        super.onDetach()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webview.apply {
            setBackgroundColor(Color.TRANSPARENT)
            webViewClient = WebViewClientCompatImpl()

            webChromeClient = WebChromeClientImpl()

            settings.apply {
                domStorageEnabled = true
                javaScriptEnabled = true
                allowFileAccess = true
                builtInZoomControls = true
                displayZoomControls = false
                setSupportZoom(true)
                javaScriptCanOpenWindowsAutomatically = true
                loadWithOverviewMode = true
                useWideViewPort = true
            }
        }

        binding.swiperefresh.apply {
            webContentCallback?.let { callback ->
                if (callback.isSwipeRefreshEnabled()) {
                    isEnabled = true
                    setOnRefreshListener {
                        reload()
                        isRefreshing = false
                    }
                }
                else {
                    isEnabled = false
                    setOnRefreshListener(null)
                }
            }
        }
    }

    inner class WebViewClientCompatImpl: WebViewClientCompat() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            switchLoadingState(true, null)
            super.onPageStarted(view, url, favicon)
        }

        //needs to be here otherwise the links are not opened or redirected to an external browser
        override fun shouldOverrideUrlLoading(
            view: WebView, request: WebResourceRequest
        ): Boolean {
            val strUrl = request.url.toString()
            if (!URLUtil.isNetworkUrl(strUrl)) {
                return true
            }
            webContentCallback?.let {
                if (it.shouldOpenUrlInExternalBrowser(view, strUrl)) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(strUrl)))
                    return true
                }
            }
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onReceivedError(
            view: WebView, request: WebResourceRequest, error: WebResourceErrorCompat
        ) {
            var shouldPropagateError = true
            if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_RESOURCE_ERROR_GET_CODE) &&
                ignoredErrorCodes.contains(error.errorCode)) {
                shouldPropagateError = false
            }

            if (shouldPropagateError) {
                //only propagate errors not contained in ignoredErrorCodes
                val showDefaultErrorMessage = webContentCallback?.onReceivedError(view, request, error) ?: false
                if (showDefaultErrorMessage) {
                    switchErrorState(true, error)
                }
            }
            switchLoadingState(false, error)
            super.onReceivedError(view, request, error)
        }

        override fun onPageFinished(view: WebView, url: String) {
            webContentCallback?.onPageFinished(view, url)
            switchLoadingState(false, null)
            super.onPageFinished(view, url)
        }
    }

    inner class WebChromeClientImpl: WebChromeClient() {
        override fun onJsConfirm(
            view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            this@WebContentFragment.context?.let { context ->
                showJsAlertDialog(context, message, result)
                return true
            }
            return false
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            consoleMessage?.let {
                webContentCallback?.onConsoleMessage(it)
            }
            return true
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            title?.let {
                webContentCallback?.onReceivedTitle(it)
            }
            super.onReceivedTitle(view, title)
        }

        override fun onPermissionRequest(request: PermissionRequest) {
            request.grant(request.resources)
        }

        private fun showJsAlertDialog(context: Context, message: String?, result: JsResult?) {
            AlertDialog.Builder(context).also { dialogBuilder ->
                dialogBuilder.setMessage(message)
                dialogBuilder.setPositiveButton(R.string.button_ok) { _, _ -> result?.confirm() }
                dialogBuilder.setNegativeButton(R.string.button_cancel) { _, _ -> result?.cancel() }
                dialogBuilder.create()
                dialogBuilder.show()
            }
        }
    }

    open fun switchLoadingState(loadingState: Boolean, error: WebResourceErrorCompat?) {
        Handler(Looper.getMainLooper()).post {
            binding.swiperefresh.isRefreshing = loadingState
        }
    }

    @SuppressLint("SetTextI18n")
    private fun switchErrorState(errorState: Boolean, error: WebResourceErrorCompat?) {
        Handler(Looper.getMainLooper()).post {
            when (errorState) {
                true -> {
                    binding.webLayout.visibility = View.GONE
                    binding.errorLayout.visibility = View.VISIBLE
                    error?.let {
                        if (WebViewFeature.isFeatureSupported(WEB_RESOURCE_ERROR_GET_DESCRIPTION)) {
                            binding.errorMessageText.text = "(${it.description})"
                        }
                    }
                    binding.errorContinueButton.setOnClickListener {
                        reload()
                    }
                }
                false -> {
                    binding.webLayout.visibility = View.VISIBLE
                    binding.errorLayout.visibility = View.GONE
                    binding.errorMessageText.text = ""
                    binding.errorContinueButton.setOnClickListener(null)
                }
            }
        }
    }

    override fun goBack(): Boolean {
        if (binding.webview.canGoBack()) {
            binding.webview.goBack()
            return true
        }
        return false
    }

    override fun getCurrentUrl(): String? {
        return binding.webview.url
    }

    override fun loadUrl(url: String) {
        switchErrorState(false, null)
        binding.webview.loadUrl(url)
    }

    override fun loadDataWithBaseURL(urlData: WebContentFragmentCallback.UrlData) {
        switchErrorState(false, null)
        binding.webview.loadDataWithBaseURL(urlData.baseUrl, urlData.data, urlData.mimeType, urlData.encoding, urlData.historyUrl)
    }

    override fun reload() {
        switchErrorState(false, null)
        // by calling reload from js (instead of webview.reload())
        // prevents the webview to add same page to history
        binding.webview.loadUrl( "javascript:window.location.reload( true )" )
    }

    companion object {
        fun newFragment(
            callback: WebContentFragmentCallback
        ): WebContentFragment {
            return WebContentFragment().apply {
                webContentCallback = callback
            }
        }
    }
}