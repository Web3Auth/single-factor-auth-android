package com.web3auth.singlefactorauth.types

import androidx.annotation.Keep
import java.io.Serializable

@Keep
enum class ErrorCode : Serializable {
    USER_ALREADY_ENABLED_MFA,
    PRIVATE_KEY_NOT_FOUND,
    RUNTIME_ERROR,
    SOMETHING_WENT_WRONG,
    CONTEXT_NOT_FOUND
}