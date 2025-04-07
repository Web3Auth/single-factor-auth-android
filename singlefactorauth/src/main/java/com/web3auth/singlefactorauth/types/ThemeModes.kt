package com.web3auth.singlefactorauth.types

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
enum class ThemeModes : Serializable {
    @SerializedName("light")
    LIGHT,

    @SerializedName("dark")
    DARK,

    @SerializedName("auto")
    AUTO
}