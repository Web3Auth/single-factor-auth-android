package com.web3auth.singlefactorauth.types

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
enum class ChainNamespace : Serializable {
    @SerializedName("eip155")
    EIP155,

    @SerializedName("solana")
    SOLANA
}