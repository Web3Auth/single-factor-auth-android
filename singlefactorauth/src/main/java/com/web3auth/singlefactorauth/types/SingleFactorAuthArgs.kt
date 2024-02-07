package com.web3auth.singlefactorauth.types

import org.torusresearch.fetchnodedetails.types.TorusNetwork

class SingleFactorAuthArgs(
    network: TorusNetwork,
    clientId: String,
    usePnPKey: Boolean = false
) {
    private var network: TorusNetwork
    var networkUrl: String? = null
    private var clientId: String? = null
    private var usePnPKey: Boolean

    init {
        this.network = network
        this.clientId = clientId
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
                    put(TorusNetwork.MAINNET, "0xf20336e16B5182637f09821c27BDe29b0AFcfe80")
                    put(TorusNetwork.TESTNET, "0xd084604e5FA387FbC2Da8bAab07fDD6aDED4614A")
                    put(TorusNetwork.CYAN, "0x9f072ba19b3370e512aa1b4bfcdaf97283168005")
                    put(TorusNetwork.AQUA, "0x29Dea82a0509153b91040ee13cDBba0f03efb625")
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