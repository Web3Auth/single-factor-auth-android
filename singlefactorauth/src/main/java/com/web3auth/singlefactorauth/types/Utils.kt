package com.web3auth.singlefactorauth.types

import android.util.Base64

const val BASE64_URL_FLAGS = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING

fun ByteArray.toBase64URLString(): String = Base64.encodeToString(this, BASE64_URL_FLAGS)

fun decodeBase64URLString(src: String): ByteArray = Base64.decode(src, BASE64_URL_FLAGS)

fun WhiteLabelData.merge(other: WhiteLabelData): WhiteLabelData {
    val mergedTheme = HashMap<String, String?>()
    this.theme.let {
        if (it != null) {
            mergedTheme.putAll(it)
        }
    }
    other.theme?.forEach { (key, value) ->
        if (!mergedTheme.containsKey(key)) {
            mergedTheme[key] = value ?: mergedTheme[key]
        }
    }

    return WhiteLabelData(
        appName = this.appName ?: other.appName,
        appUrl = this.appUrl ?: other.appUrl,
        logoLight = this.logoLight ?: other.logoLight,
        logoDark = this.logoDark ?: other.logoDark,
        defaultLanguage = this.defaultLanguage ?: other.defaultLanguage,
        mode = this.mode ?: other.mode,
        useLogoLoader = this.useLogoLoader ?: other.useLogoLoader,
        theme = mergedTheme
    )
}

fun Map<String, String>?.mergeMaps(other: Map<String, String>?): Map<String, String>? {
    if (this == null && other == null) {
        return null
    } else if (this == null) {
        return other
    } else if (other == null) {
        return this
    }

    val mergedMap = LinkedHashMap<String, String>()
    mergedMap.putAll(this)

    other.forEach { (key, value) ->
        mergedMap[key] = value
    }

    return mergedMap
}