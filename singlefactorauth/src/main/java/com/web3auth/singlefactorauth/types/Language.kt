package com.web3auth.singlefactorauth.types

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
enum class Language : Serializable {
    @SerializedName("en") //English
    EN,

    @SerializedName("de") //German
    DE,

    @SerializedName("ja") //Japanese
    JA,

    @SerializedName("ko") //Korean
    KO,

    @SerializedName("zh") //Mandarin
    ZH,

    @SerializedName("es") //Spanish
    ES,

    @SerializedName("fr") //French
    FR,

    @SerializedName("pt") //Portugese
    PT,

    @SerializedName("nl") //Dutch
    NL,

    @SerializedName("tr") //Turkish
    TR,
}