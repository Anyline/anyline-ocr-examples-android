package com.anyline.examples.viewConfigEditor

interface WebContentFragmentAction {
    fun goBack(): Boolean

    fun getCurrentUrl(): String?

    fun loadUrl(url: String)

    fun loadDataWithBaseURL(urlData: WebContentFragmentCallback.UrlData)

    fun reload()

}