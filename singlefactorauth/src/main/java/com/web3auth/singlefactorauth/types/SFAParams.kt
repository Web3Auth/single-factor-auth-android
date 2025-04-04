package com.web3auth.singlefactorauth.types

import android.net.Uri
import androidx.annotation.Keep
import org.torusresearch.fetchnodedetails.types.Web3AuthNetwork
import java.io.Serializable

@Keep
data class Web3AuthOptions(
    @Keep var clientId: String,
    @Keep var web3AuthNetwork: Web3AuthNetwork,
    @Keep var sessionTime: Int = 86400,
    @Keep var serverTimeOffset: Int = 0,
    @Keep var storageServerUrl: String? = null,
    @Keep var whiteLabel: WhiteLabelData? = null,
    @Keep var originData: Map<String, String>? = null,
    @Keep var buildEnv: BuildEnv? = BuildEnv.PRODUCTION,
    @Keep @Transient var redirectUrl: Uri? = null,
    @Keep var walletSdkUrl: String? = getWalletSdkUrl(buildEnv)
) : Serializable {
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

const val walletServicesVersion = "v4"
const val WEBVIEW_URL = "walletUrl"
const val REDIRECT_URL = "redirectUrl"
