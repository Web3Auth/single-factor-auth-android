package com.web3auth.singlefactorauth.utils

import java.io.StringReader

class WellKnownSecret {
    companion object {
        fun pem(): StringReader {
            return StringReader(
                """-----BEGIN EC PRIVATE KEY-----
        MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCCD7oLrcKae+jVZPGx52Cb/lKhdKxpXjl9eGNa1MlY57A==
                -----END EC PRIVATE KEY-----"""
            )
        }
    }
}