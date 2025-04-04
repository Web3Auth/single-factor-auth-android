package com.web3auth.singlefactorauth.types

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
enum class Network : Serializable {
    @SerializedName("mainnet")
    MAINNET,

    @SerializedName("testnet")
    TESTNET,

    @SerializedName("cyan")
    CYAN,

    @SerializedName("aqua")
    AQUA,

    @SerializedName("sapphire_devnet")
    SAPPHIRE_DEVNET,

    @SerializedName("sapphire_mainnet")
    SAPPHIRE_MAINNET
}