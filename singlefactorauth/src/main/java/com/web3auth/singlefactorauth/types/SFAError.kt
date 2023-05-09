package com.web3auth.singlefactorauth.types

object SFAError {

    fun getError(errorCode: ErrorCode): String {
        return when (errorCode) {
            ErrorCode.USER_ALREADY_ENABLED_MFA -> {
                "User has already enabled MFA"
            }
            ErrorCode.PRIVATE_KEY_NOT_FOUND -> {
                "Unable to get private key from torus nodes"
            }
            ErrorCode.SOMETHING_WENT_WRONG -> {
                "Something went wrong!"
            }
            ErrorCode.RUNTIME_ERROR -> {
                "Runtime Error"
            }
        }
    }
}

enum class ErrorCode {
    USER_ALREADY_ENABLED_MFA,
    PRIVATE_KEY_NOT_FOUND,
    RUNTIME_ERROR,
    SOMETHING_WENT_WRONG,
}