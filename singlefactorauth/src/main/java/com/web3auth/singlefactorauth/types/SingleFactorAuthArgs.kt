package com.web3auth.singlefactorauth.types

import org.torusresearch.fetchnodedetails.FetchNodeDetails
import org.torusresearch.fetchnodedetails.types.TorusNetwork

class SingleFactorAuthArgs(
    network: TorusNetwork,
    clientId: String? = null,
    usePnPKey: Boolean = false
) {
    private var network: TorusNetwork
    var networkUrl: String? = null
    private var clientId: String? = null
    private var usePnPKey: Boolean

    init {
        this.network = network
        if (clientId != null) {
            this.clientId = clientId
        }
        this.usePnPKey = usePnPKey
    }

    fun getNetwork(): TorusNetwork {
        return network
    }

    fun setNetwork(network: TorusNetwork) {
        this.network = network
    }

    fun getClientId(): String? {
        return clientId
    }

    fun setClientId(clientId: String) {
        this.clientId = clientId
    }

    fun getUsePnPKey(): Boolean {
        return usePnPKey
    }

    companion object {
        var CONTRACT_MAP: HashMap<TorusNetwork?, String?> =
            object : HashMap<TorusNetwork?, String?>() {
                init {
                    put(TorusNetwork.MAINNET, FetchNodeDetails.PROXY_ADDRESS_MAINNET)
                    put(TorusNetwork.TESTNET, FetchNodeDetails.PROXY_ADDRESS_TESTNET)
                    put(TorusNetwork.CYAN, FetchNodeDetails.PROXY_ADDRESS_CYAN)
                    put(TorusNetwork.AQUA, FetchNodeDetails.PROXY_ADDRESS_AQUA)
                }
            }
        var SIGNER_MAP: HashMap<TorusNetwork?, String?> = object : HashMap<TorusNetwork?, String?>() {
            init {
                put(TorusNetwork.MAINNET, "https://signer.tor.us")
                put(TorusNetwork.TESTNET, "https://signer.tor.us")
                put(TorusNetwork.CYAN, "https://signer-polygon.tor.us")
                put(TorusNetwork.AQUA, "https://signer-polygon.tor.us")
                put(TorusNetwork.SAPPHIRE_MAINNET, "https://signer.tor.us")
                put(TorusNetwork.SAPPHIRE_DEVNET, "https://signer.tor.us")
            }
        }
    }
}