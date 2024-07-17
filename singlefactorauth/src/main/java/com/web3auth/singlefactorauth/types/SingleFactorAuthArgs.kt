package com.web3auth.singlefactorauth.types

import org.torusresearch.fetchnodedetails.types.Web3AuthNetwork

class SingleFactorAuthArgs(network: Web3AuthNetwork, clientid: String) {
    private var network: Web3AuthNetwork
    var clientid: String
    var networkUrl: String? = null

    init {
        this.network = network
        this.clientid = clientid
    }

    fun getNetwork(): Web3AuthNetwork {
        return network
    }

    fun setNetwork(network: Web3AuthNetwork) {
        this.network = network
    }

    companion object {
        var SIGNER_MAP: HashMap<Web3AuthNetwork?, String?> =
            object : HashMap<Web3AuthNetwork?, String?>() {
            init {
                put(Web3AuthNetwork.MAINNET, "https://signer.tor.us")
                put(Web3AuthNetwork.TESTNET, "https://signer.tor.us")
                put(Web3AuthNetwork.CYAN, "https://signer-polygon.tor.us")
                put(Web3AuthNetwork.AQUA, "https://signer-polygon.tor.us")
            }
        }
    }
}