package com.web3auth.singlefactorauth.types

import org.torusresearch.fetchnodedetails.types.Web3AuthNetwork

class Web3AuthOptions(
    private var clientId: String,
    private var web3AuthNetwork: Web3AuthNetwork,
    private var sessionTime: Int = 86400,
    private var serverTimeOffset: Int = 0,
    private var storageServerUrl: String? = null
) {

    init {
        serverTimeOffset = serverTimeOffset ?: 0
    }

    fun getClientId(): String {
        return clientId
    }

    fun getNetwork(): Web3AuthNetwork {
        return web3AuthNetwork
    }

    fun getServerTimeOffset(): Int {
        return serverTimeOffset
    }

    fun getStorageServerUrl(): String? {
        return storageServerUrl
    }

    fun setNetwork(network: Web3AuthNetwork) {
        this.web3AuthNetwork = network
    }

    fun getSessionTime(): Int {
        return sessionTime
    }
}
