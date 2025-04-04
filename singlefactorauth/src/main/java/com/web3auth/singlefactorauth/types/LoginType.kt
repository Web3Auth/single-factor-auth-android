package com.web3auth.singlefactorauth.types

import androidx.annotation.Keep
import java.io.Serializable

@Keep
enum class LoginType : Serializable {
    google,
    facebook,
    discord,
    reddit,
    twitch,
    apple,
    github,
    linkedin,
    twitter,
    weibo,
    line,
    email_password,
    email_passwordless,
    sms_passwordless,
    jwt;
}