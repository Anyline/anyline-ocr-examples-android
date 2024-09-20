package com.anyline.examples.viewConfigEditor

import android.content.Context
import android.webkit.ConsoleMessage
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.webkit.WebResourceErrorCompat

interface WebContentFragmentCallback {
    data class UrlData(val data: String,
                       val baseUrl: String?,
                       val mimeType: String? = "text/html",
                       val encoding: String? = "UTF-8",
                       val historyUrl: String? = null)
    fun getHomeUrl(context: Context): String?
    fun getHomeUrlData(context: Context): UrlData?
    fun isSwipeRefreshEnabled(): Boolean
    fun isFileChooserEnabled(): Boolean
    fun shouldOpenUrlInExternalBrowser(view: WebView, url: String): Boolean

    fun onPageFinished(view: WebView, url: String)
    fun onReceivedError(webView: WebView,
                        webResourceRequest: WebResourceRequest,
                        webResourceErrorCompat: WebResourceErrorCompat): Boolean

    fun onReceivedTitle(title: String)
    fun onConsoleMessage(consoleMessage: ConsoleMessage)
}