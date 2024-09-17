package com.web3auth.singlefactorauth.types

import org.torusresearch.fetchnodedetails.types.Web3AuthNetwork

class SFAParams(
    private var network: Web3AuthNetwork,
    clientId: String,
    networkUrl: String? = null,
    serverTimeOffset: Int = 0,
    sessionTime: Int = 86400
) {
    private var clientId: String = "torus-default"
    private var networkUrl: String? = networkUrl
    private var enableOneKey: Boolean
    private var serverTimeOffset: Int = serverTimeOffset
    private var sessionTime: Int = sessionTime


    init {
        this.clientId = clientId
        this.enableOneKey = true
        this.networkUrl = networkUrl
        this.serverTimeOffset = serverTimeOffset
        this.sessionTime = sessionTime
    }

    fun getClientId(): String {
        return clientId
    }
    fun getNetwork(): Web3AuthNetwork {
        return network
    }

    fun getNetworkUrl(): String? {
        return networkUrl
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