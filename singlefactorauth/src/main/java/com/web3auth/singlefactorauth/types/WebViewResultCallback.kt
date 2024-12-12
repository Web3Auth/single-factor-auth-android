package com.web3auth.singlefactorauth.types

interface WebViewResultCallback {
    fun onSignResponseReceived(signResponse: SignResponse?)
    fun onWebViewCancelled()
}