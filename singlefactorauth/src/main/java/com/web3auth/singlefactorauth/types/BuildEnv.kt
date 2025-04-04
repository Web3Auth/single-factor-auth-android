package com.web3auth.singlefactorauth.types

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
enum class BuildEnv : Serializable {
    @SerializedName("production")
    PRODUCTION,

    @SerializedName("staging")
    STAGING,

    @SerializedName("testing")
    TESTING
}