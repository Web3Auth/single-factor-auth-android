package com.web3auth.singlefactorauth.types

import org.torusresearch.fetchnodedetails.types.Web3AuthNetwork

class SingleFactorAuthArgs(network: Web3AuthNetwork, clientid: String, networkUrl: String? = null, serverTimeOffset: Int = 0) {
    private var network: Web3AuthNetwork
    private var clientid: String = "torus-default"
    private var networkUrl: String? = null
    private var enableOneKey: Boolean
    private var serverTimeOffset: Int = 0


    init {
        this.network = network
        this.clientid = clientid
        this.enableOneKey = true
    }
    fun getClientId(): String? {
        return clientid
    }
    fun getNetwork(): Web3AuthNetwork {
        return network
    }

    fun getNetworkUrl(): String? {
        return networkUrl
    }
    fun get(): Web3AuthNetwork {
        return network
    }

    fun getEnableOneKey(): Boolean {
        return enableOneKey
    }

    fun getServerTimeOffset(): Int {
        return serverTimeOffset
    }
}