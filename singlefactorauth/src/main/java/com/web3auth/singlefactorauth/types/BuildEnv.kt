package com.web3auth.singlefactorauth.types

import com.google.gson.annotations.SerializedName

enum class BuildEnv {
    @SerializedName("production")
    PRODUCTION,

    @SerializedName("staging")
    STAGING,

    @SerializedName("testing")
    TESTING
}