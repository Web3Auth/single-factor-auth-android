package com.web3auth.singlefactorauth.types

import org.torusresearch.fetchnodedetails.types.Web3AuthNetwork

class SFAParams(
    private var network: Web3AuthNetwork,
    clientId: String,
    sessionTime: Int = 86400,
    legacyMetaDataHostUrl: String? = null,
    serverTimeOffset: Int = 0,
) {
    private var clientId: String = "torus-default"
    private var legacyMetaDataHostUrl: String? = legacyMetaDataHostUrl
    private var enableOneKey: Boolean
    private var serverTimeOffset: Int = serverTimeOffset
    private var sessionTime: Int = sessionTime


    init {
        this.clientId = clientId
        this.enableOneKey = true
        this.sessionTime = sessionTime
        this.legacyMetaDataHostUrl = legacyMetaDataHostUrl
        this.serverTimeOffset = serverTimeOffset
    }

    fun getClientId(): String {
        return clientId
    }
    fun getNetwork(): Web3AuthNetwork {
        return network
    }

    fun getLegacyMetaDataHostUrl(): String? {
        return legacyMetaDataHostUrl
    }

    fun getEnableOneKey(): Boolean {
        return enableOneKey
    }

    fun getServerTimeOffset(): Int {
        return serverTimeOffset
    }

    fun getSessionTime(): Int {
        return sessionTime
    }
}