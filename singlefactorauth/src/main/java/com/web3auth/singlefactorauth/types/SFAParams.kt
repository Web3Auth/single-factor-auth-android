package com.web3auth.singlefactorauth.types

import android.net.Uri
import org.torusresearch.fetchnodedetails.types.Web3AuthNetwork

data class Web3AuthOptions(
    var clientId: String,
    var web3AuthNetwork: Web3AuthNetwork,
    var sessionTime: Int = 86400,
    var serverTimeOffset: Int = 0,
    var storageServerUrl: String? = null,
    var whiteLabel: WhiteLabelData? = null,
    var originData: Map<String, String>? = null,
    var buildEnv: BuildEnv? = BuildEnv.PRODUCTION,
    @Transient var redirectUrl: Uri? = null,
    var walletSdkUrl: String? = getWalletSdkUrl(buildEnv)
) {
    init {
        serverTimeOffset = serverTimeOffset ?: 0
    }
}

fun getWalletSdkUrl(buildEnv: BuildEnv?): String {
    val sdkUrl: String = when (buildEnv) {
        BuildEnv.STAGING -> {
            "https://staging-wallet.web3auth.io/$walletServicesVersion"
        }

        BuildEnv.TESTING -> {
            "https://develop-wallet.web3auth.io"
        }

        else -> {
            "https://wallet.web3auth.io/$walletServicesVersion"
        }
    }
    return sdkUrl
}

const val walletServicesVersion = "v3"
const val WEBVIEW_URL = "walletUrl"
const val REDIRECT_URL = "redirectUrl"
